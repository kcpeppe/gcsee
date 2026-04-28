// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.jvm;

import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.parser.datatype.TripleState;
import com.kodewerk.gcsee.time.DateTimeStamp;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static com.kodewerk.gcsee.aggregator.EventSource.SAFEPOINT;
import static com.kodewerk.gcsee.aggregator.EventSource.SURVIVOR;
import static com.kodewerk.gcsee.jvm.SupportedFlags.*;

/*
    Index guide

    APPLICATION_STOPPED_TIME,                   //  0
    APPLICATION_CONCURRENT_TIME,                //  1

    DEFNEW,                                     //  2
    PARNEW,                                     //  3
    CMS,                                        //  4
    ICMS,                                       //  5
    PARALLELGC,                                 //  6
    PARALLELOLDGC,                              //  7
    SERIAL,                                     //  8
    G1GC,                                       //  9
    ZGC,                                        // 10
    SHENANDOAH,                                 // 11

    GC_DETAILS,                                 // 12
    TENURING_DISTRIBUTION,                      // 13
    GC_CAUSE,                                   // 14
    CMS_DEBUG_LEVEL_1,                          // 15
    ADAPTIVE_SIZING,                            // 16

    JDK70,                                      // 17
    PRE_JDK70_40,                               // 18
    JDK80,                                      // 19
    UNIFIED_LOGGING,                            // 20

    PRINT_HEAP_AT_GC,                           // 21
    RSET_STATS,                                 // 22

    PRINT_REFERENCE_GC,                         // 23
    MAX_TENURING_THRESHOLD_VIOLATION,           // 24
    TLAB_DATA,                                  // 25
    PRINT_PROMOTION_FAILURE,                    // 26
    PRINT_FLS_STATISTICS                        // 27
    PRINT_CPU_TIMES                             // 28
    GENERATIONAL_ZGC                            // 29
    ZERO_GCID                                   // 30
 */

public class Diary {

    /**
     * Absolute floor (seconds) below which we always treat the log as
     * untruncated and report {@link #getEstimatedStartTime()} as
     * {@link DateTimeStamp#baseDate()}. Picked to be larger than any realistic
     * JVM warm-up before the first GC event; GC log rotation policies very
     * rarely keep more than two minutes of pre-rotation history out of the
     * front of the log. Used only on the {@link SupportedFlags#ZERO_GCID}
     * UNKNOWN branch (pre-unified logs).
     */
    private static final double EFFECTIVE_LOG_START_EPSILON_SECONDS = 120.0;

    /**
     * Multiplier on the observed mean inter-event interval; if the first log
     * line is within this many typical event gaps of the JVM origin the log
     * is treated as untruncated even when it sits beyond
     * {@link #EFFECTIVE_LOG_START_EPSILON_SECONDS}. Guards sparse-GC workloads
     * where two minutes is a normal idle gap between events. Used only on the
     * {@link SupportedFlags#ZERO_GCID} UNKNOWN branch.
     */
    private static final double FIRST_INTERVAL_TRUNCATION_MULTIPLIER = 5.0;

    /**
     * Number of inter-event intervals we average when back-extrapolating the
     * JVM start time from the first observed event on a (presumed-truncated)
     * log. Five gives a stable mean without leaning on long-tail outliers.
     */
    private static final int FIRST_INTERVAL_SAMPLE_SIZE = 5;

    private final TripleState[] states;
    private DateTimeStamp timeOfFirstEvent;

    /** Sum of the first {@link #FIRST_INTERVAL_SAMPLE_SIZE} non-negative inter-event intervals. */
    private double sumOfFirstIntervals = 0.0;
    /** Number of intervals contributing to {@link #sumOfFirstIntervals}, capped at the sample size. */
    private int countOfFirstIntervals = 0;
    /** Previous event's timestamp, used to compute the next interval. */
    private DateTimeStamp previousEventTimeStamp = null;
    /** Computed by {@link #finalise()} once the diarizer pre-pass completes. */
    private DateTimeStamp estimatedStartTime = null;
    /** Guards {@link #finalise()} idempotency. */
    private boolean finalised = false;

    public Diary() {
        states = new TripleState[SupportedFlags.values().length];
        for (int i = 0; i < states.length; i++) states[i] = TripleState.UNKNOWN;
    }

    public void setTrue(SupportedFlags flag) {
        if (states[flag.ordinal()] == TripleState.UNKNOWN) states[flag.ordinal()] = TripleState.TRUE;
    }

    public void setTrue(SupportedFlags... flags) {
        for (SupportedFlags flag : flags) {
            setTrue(flag);
        }
    }

    public void setFalse(SupportedFlags flag) {
        if (states[flag.ordinal()] == TripleState.UNKNOWN) states[flag.ordinal()] = TripleState.FALSE;
    }

    public void setFalse(SupportedFlags... flags) {
        for (SupportedFlags flag : flags) {
            setFalse(flag);
        }
    }

    public boolean isStateKnown(SupportedFlags flag) {
        return states[flag.ordinal()].isKnown();
    }

    public boolean isStateKnown(SupportedFlags... flags) {
        boolean value = true;
        for (SupportedFlags flag : flags) {
            value &= states[flag.ordinal()].isTrue();
        }
        return value;
    }

    public void setState(SupportedFlags flag, boolean flagTurnedOn) {
        if ((flagTurnedOn)) {
            setTrue(flag);
        } else {
            setFalse(flag);
        }
    }

    public boolean isTrue(SupportedFlags flag) {
        return (isStateKnown(flag)) && states[flag.ordinal()].isTrue();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("LoggingDiary{");
        boolean first = true;
        for(SupportedFlags flag : SupportedFlags.values()) {
            if (!first || (first = false)) {
                buffer.append(", ");
            }
            buffer.append(flag.name()).append("=").append(states[flag.ordinal()]);
        }
        return buffer.append("}").toString();
    }

    public boolean isComplete() {
        return Arrays.stream(states).allMatch(TripleState::isKnown);
    }

    public boolean isCollectorKnown() {
        return isGenerationalKnown() || isG1GCKnown() || isZGCKnown() || isShenandoahKnown();
    }

    public boolean isVersionKnown() {
        return isUnifiedLogging() || isJDK80() || (isJDK70() && isPre70_40Known());

    }

    public boolean isDetailsKnown() {
        return isApplicationStoppedTimeKnown() && isPrintReferenceGCKnown() && isPrintGCDetailsKnown() &&
                isAdaptiveSizingKnown() && isTLABDataKnown() && isPrintPromotionFailureKnown() &&
                isPrintFLSStatisticsKnown() && isPrintHeapAtGCKnown();
    }

    public boolean isDefNew() {
        return isTrue(SupportedFlags.DEFNEW);
    }

    public boolean isSerialFull() {
        return isTrue(SupportedFlags.SERIAL);
    }

    public boolean isParNew() {
        return isTrue(PARNEW);
    }

    public boolean isCMS() {
        return isTrue(SupportedFlags.CMS);
    }

    public boolean isICMS() {
        return isTrue(SupportedFlags.ICMS);
    }

    public boolean isPSYoung() {
        return isTrue(SupportedFlags.PARALLELGC);
    }

    public boolean isPSOldGen() {
        return isTrue(SupportedFlags.PARALLELOLDGC);
    }

    public boolean isG1GC() {
        return isTrue(SupportedFlags.G1GC);
    }

    public boolean isZGC() {
        return isTrue(SupportedFlags.ZGC);
    }

    public boolean isGenerationalZGC(){
        return isTrue(SupportedFlags.GENERATIONAL_ZGC);
    }

    public boolean isShenandoah() {
        return isTrue(SupportedFlags.SHENANDOAH);
    }

    public boolean isGenerational() {
        return isCollectorKnown() && !(isG1GC() || isZGC() || isShenandoah());
    }

    public boolean isPrintGCDetails() {
        return isTrue(SupportedFlags.GC_DETAILS);
    }

    public boolean isTenuringDistribution() {
        return isTrue(SupportedFlags.TENURING_DISTRIBUTION);
    }

    public boolean isGCCause() {
        return isTrue(SupportedFlags.GC_CAUSE);
    }

    public boolean isAdaptiveSizing() {
        return isTrue(SupportedFlags.ADAPTIVE_SIZING);
    }

    public boolean isCMSDebugLevel1() {
        return isTrue(SupportedFlags.CMS_DEBUG_LEVEL_1);
    }

    public boolean isApplicationStoppedTime() {
        return isTrue(APPLICATION_STOPPED_TIME);
    }

    public boolean isApplicationRunningTime() {
        return isTrue(APPLICATION_CONCURRENT_TIME);
    }

    public boolean isTLABData() {
        return isTrue(SupportedFlags.TLAB_DATA);
    }

    public boolean isTLABDataKnown() {
        return isStateKnown(SupportedFlags.TLAB_DATA);
    }

    public boolean isJDK70() {
        return isTrue(SupportedFlags.JDK70);
    }

    public boolean isPre70_40() {
        return isTrue(SupportedFlags.PRE_JDK70_40);
    } // GCCause with perm is 7.0, (System) is pre _40, System.gc() is _40+

    public boolean isPre70_40Known() {
        return isStateKnown(SupportedFlags.PRE_JDK70_40);
    }

    public boolean isJDK80() {
        return isTrue(SupportedFlags.JDK80);
    } //look for metaspace record...

    public boolean isUnifiedLogging() {
        return isTrue(SupportedFlags.UNIFIED_LOGGING);
    }

    public boolean isPrintHeapAtGC() {
        return isTrue(SupportedFlags.PRINT_HEAP_AT_GC);
    }

    public boolean isRSetStats() {
        return isTrue(SupportedFlags.RSET_STATS);
    }

    public boolean hasPrintReferenceGC() {
        return isTrue(SupportedFlags.PRINT_REFERENCE_GC);
    }

    public boolean isMaxTenuringThresholdViolation() {
        return isTrue(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
    }

    public boolean isDefNewKnown() {
        return isStateKnown(SupportedFlags.DEFNEW);
    }

    public boolean isSerialFullKnown() {
        return isStateKnown(SupportedFlags.SERIAL);
    }

    public boolean isParNewKnown() {
        return isStateKnown(PARNEW);
    }

    public boolean isCMSKnown() {
        return isStateKnown(SupportedFlags.CMS);
    }

    public boolean isICMSKnown() {
        return isStateKnown(SupportedFlags.ICMS);
    }

    public boolean isPSYoungKnown() {
        return isStateKnown(SupportedFlags.PARALLELGC);
    }

    public boolean isPSOldGenKnown() {
        return isStateKnown(SupportedFlags.PARALLELOLDGC);
    }

    public boolean isG1GCKnown() {
        return isStateKnown(SupportedFlags.G1GC);
    }

    public boolean isZGCKnown() {
        return isStateKnown(SupportedFlags.ZGC);
    }

    public boolean isShenandoahKnown() {
        return isStateKnown(SupportedFlags.SHENANDOAH);
    }

    public boolean isGenerationalKnown() {
        return isStateKnown(SupportedFlags.G1GC);
    }

    public boolean isPrintGCDetailsKnown() {
        return isStateKnown(SupportedFlags.GC_DETAILS);
    }

    public boolean isTenuringDistributionKnown() {
        return isStateKnown(SupportedFlags.TENURING_DISTRIBUTION);
    }

    public boolean isGCCauseKnown() {
        return isStateKnown(SupportedFlags.GC_CAUSE);
    }

    public boolean isAdaptiveSizingKnown() {
        return isStateKnown(SupportedFlags.ADAPTIVE_SIZING);
    }

    public boolean isCMSDebugLevel1Known() {
        return isStateKnown(SupportedFlags.CMS_DEBUG_LEVEL_1);
    }

    public boolean isApplicationStoppedTimeKnown() {
        return isStateKnown(APPLICATION_STOPPED_TIME);
    }

    public boolean isApplicationRunningTimeKnown() {
        return isStateKnown(APPLICATION_CONCURRENT_TIME);
    }

    public boolean isJDK70Known() {
        return isStateKnown(SupportedFlags.JDK70);
    }

    public boolean isPre70_45Known() {
        return isStateKnown(SupportedFlags.PRE_JDK70_40);
    } // GCCause with perm is 7.0, (System) is pre _45, System.gc() is _45+

    public boolean isJDK80Known() {
        return isStateKnown(SupportedFlags.JDK80);
    } //look for metaspace record...

    public boolean isUnifiedLoggingKnown() {
        return isStateKnown(SupportedFlags.UNIFIED_LOGGING);
    } //Unsure how to know this for sure.

    public boolean isPrintHeapAtGCKnown() {
        return isStateKnown(SupportedFlags.PRINT_HEAP_AT_GC);
    }

    public boolean isRSetStatsKnown() {
        return isStateKnown(SupportedFlags.RSET_STATS);
    }

    public boolean isPrintReferenceGCKnown() {
        return isStateKnown(SupportedFlags.PRINT_REFERENCE_GC);
    }

    public boolean isPrintPromotionFailure() {
        return isTrue(SupportedFlags.PRINT_PROMOTION_FAILURE);
    }

    public boolean isPrintPromotionFailureKnown() {
        return isStateKnown(SupportedFlags.PRINT_PROMOTION_FAILURE);
    }

    public boolean isPrintFLSStatistics() {
        return isTrue(SupportedFlags.PRINT_FLS_STATISTICS);
    }

    public boolean isPrintFLSStatisticsKnown() {
        return isStateKnown(SupportedFlags.PRINT_FLS_STATISTICS);
    }

    public boolean isMaxTenuringThresholdViolationKnown() {
        return isStateKnown(SupportedFlags.MAX_TENURING_THRESHOLD_VIOLATION);
    }

    public boolean isZeroGCIDKnown() {
        return isStateKnown(ZERO_GCID);
    }

    /**
     * @return {@code true} when the diarizer observed {@code GC(0)} on the first
     * unified-log line and so the log is known to start from the JVM's first
     * collection. Returns {@code false} both when ZERO_GCID is FALSE and when
     * it is UNKNOWN — guard with {@link #isZeroGCIDKnown()} if the distinction
     * matters.
     */
    public boolean isZeroGCID() {
        return isTrue(ZERO_GCID);
    }

    public boolean isJVMEventsKnown() {
        return isApplicationStoppedTimeKnown() && isApplicationRunningTime();
    }

    public boolean isPrintCPUTimes() {
    	return isStateKnown(SupportedFlags.PRINT_CPU_TIMES);
    }
    
    public void setTimeOfFirstEvent(DateTimeStamp startTime) {
        if ( this.timeOfFirstEvent == null)
            this.timeOfFirstEvent = startTime;
    }

    public DateTimeStamp getTimeOfFirstEvent() {
        return this.timeOfFirstEvent;
    }

    public boolean hasTimeOfFirstEvent() {
        return this.timeOfFirstEvent != null;
    }

    /**
     * Forwarded to by the diarizer for every parsed line in the pre-pass.
     * Captures the first observed event time and accumulates the first
     * {@link #FIRST_INTERVAL_SAMPLE_SIZE} non-negative inter-event intervals,
     * both of which feed the start-time estimate computed by
     * {@link #finalise()}.
     * <p>
     * Date-only stamps (unified logs without uptime decoration) are still
     * recorded as {@code timeOfFirstEvent} so downstream consumers like
     * {@link com.kodewerk.gcsee.parser.GCLogParser GCLogParser} can seed
     * their clock; the interval accumulator picks them up too because
     * {@link DateTimeStamp#minus(DateTimeStamp)} falls back to date-stamp
     * arithmetic when uptime isn't available.
     *
     * @param eventTime the parsed timestamp of the current line. {@code null}
     *                  or fully-empty values (no date and no uptime) are
     *                  ignored.
     */
    public void recordEventTimestamp(DateTimeStamp eventTime) {
        if (eventTime == null) {
            return;
        }
        if (!eventTime.hasTimeStamp() && !eventTime.hasDateStamp()) {
            return;
        }
        if (this.timeOfFirstEvent == null) {
            this.timeOfFirstEvent = eventTime;
        }
        if (previousEventTimeStamp != null
                && countOfFirstIntervals < FIRST_INTERVAL_SAMPLE_SIZE) {
            double interval = eventTime.minus(previousEventTimeStamp);
            if (interval >= 0.0 && !Double.isNaN(interval)) {
                sumOfFirstIntervals += interval;
                countOfFirstIntervals++;
            }
        }
        previousEventTimeStamp = eventTime;
    }

    /**
     * Called by the diarizer at the end of its pre-pass, once every line has
     * been examined and the {@link SupportedFlags#ZERO_GCID} verdict is settled.
     * Computes and caches the JVM start time so that subsequent calls to
     * {@link #getEstimatedStartTime()} are pure getters.
     * <p>
     * Idempotent — repeated calls are no-ops.
     */
    public void finalise() {
        if (finalised) {
            return;
        }
        estimatedStartTime = computeEstimatedStartTime();
        finalised = true;
    }

    /**
     * Estimates the JVM start time from the data captured during the diarizer
     * pre-pass.
     *
     * <ul>
     *   <li><b>{@link SupportedFlags#ZERO_GCID} TRUE</b> — first line is
     *       {@code GC(0)}; the log starts at the JVM's first collection.
     *       Returns {@link DateTimeStamp#baseDate()}.</li>
     *   <li><b>{@link SupportedFlags#ZERO_GCID} FALSE</b> — first line is
     *       {@code GC(N>0)} on a unified log; the JVM was already running
     *       before we saw any line. Back-extrapolates as
     *       {@code timeOfFirstEvent − meanInterval} when intervals are available;
     *       otherwise falls back to {@code timeOfFirstEvent}.</li>
     *   <li><b>{@link SupportedFlags#ZERO_GCID} UNKNOWN</b> — pre-unified log
     *       (no GC id field). Applies the heuristic
     *       {@code firstUptime ≤ max(ε, K · meanInterval)} with
     *       {@code ε = }{@value #EFFECTIVE_LOG_START_EPSILON_SECONDS} seconds and
     *       {@code K = }{@value #FIRST_INTERVAL_TRUNCATION_MULTIPLIER}: under
     *       the threshold the log is treated as untruncated and the result is
     *       {@code baseDate()}; over it, back-extrapolation is attempted as in
     *       the FALSE branch.</li>
     * </ul>
     *
     * Falls back to {@code baseDate()} when no events were observed at all.
     *
     * @return The estimated JVM start time
     */
    public DateTimeStamp getEstimatedStartTime() {
        if (!finalised) {
            // Defensive: if a caller raced ahead of the diarizer pre-pass we
            // still produce an answer rather than NPE. Once finalise() runs,
            // the cached value sticks.
            finalise();
        }
        return estimatedStartTime;
    }

    private DateTimeStamp computeEstimatedStartTime() {
        if (timeOfFirstEvent == null || !timeOfFirstEvent.hasTimeStamp()) {
            return DateTimeStamp.baseDate();
        }
        final boolean haveIntervals = countOfFirstIntervals > 0;
        final double meanInterval = haveIntervals
                ? sumOfFirstIntervals / countOfFirstIntervals
                : 0.0;

        // ZERO_GCID TRUE: first line is GC(0), log is anchored at the JVM origin.
        if (isStateKnown(ZERO_GCID) && isTrue(ZERO_GCID)) {
            return DateTimeStamp.baseDate();
        }

        // ZERO_GCID FALSE: first line is GC(N>0), log is a rotated tail.
        if (isStateKnown(ZERO_GCID) && !isTrue(ZERO_GCID)) {
            return backExtrapolate(meanInterval, haveIntervals);
        }

        // ZERO_GCID UNKNOWN: pre-unified log; fall back to ε/K heuristic.
        final double firstUptime = timeOfFirstEvent.toSeconds();
        final double threshold = haveIntervals
                ? Math.max(EFFECTIVE_LOG_START_EPSILON_SECONDS,
                           FIRST_INTERVAL_TRUNCATION_MULTIPLIER * meanInterval)
                : EFFECTIVE_LOG_START_EPSILON_SECONDS;
        if (firstUptime <= threshold) {
            return DateTimeStamp.baseDate();
        }
        return backExtrapolate(meanInterval, haveIntervals);
    }

    private DateTimeStamp backExtrapolate(double meanInterval, boolean haveIntervals) {
        if (haveIntervals) {
            DateTimeStamp estimate = timeOfFirstEvent.minus(meanInterval);
            // DateTimeStamp clamps negative timestamps to NaN; if extrapolation
            // wraps below zero we cannot back-extrapolate and fall through.
            if (estimate.hasTimeStamp()) {
                return estimate;
            }
        }
        return timeOfFirstEvent;
    }

/*
    GENERATIONAL,
    CMS,
    G1GC,
    SHENANDOAH,
    ZGC,
    SAFEPOINT,
    SURVIVOR,
    TENURED;
 */
    private void evaluate(Set<EventSource> events, SupportedFlags flag, EventSource eventSource) {
        if ( isStateKnown(flag) & isTrue(flag))
            events.add(eventSource);
    }
    public Set<EventSource> generatesEvents() {
        Set<EventSource> generatedEvents = new TreeSet<>();
        evaluate(generatedEvents, APPLICATION_STOPPED_TIME, SAFEPOINT);
        evaluate(generatedEvents, APPLICATION_CONCURRENT_TIME, SAFEPOINT);
        evaluate(generatedEvents, DEFNEW, EventSource.GENERATIONAL);
        evaluate(generatedEvents, PARNEW, EventSource.GENERATIONAL);
        if (isUnifiedLogging())
            evaluate(generatedEvents, CMS, EventSource.CMS_UNIFIED);
        else
            evaluate(generatedEvents, CMS, EventSource.CMS_PREUNIFIED);
        evaluate(generatedEvents, ICMS, EventSource.CMS_PREUNIFIED);
        evaluate(generatedEvents, PARALLELGC, EventSource.GENERATIONAL);
        evaluate(generatedEvents, PARALLELOLDGC, EventSource.GENERATIONAL);
        evaluate(generatedEvents, SERIAL, EventSource.GENERATIONAL);
        evaluate(generatedEvents, G1GC, EventSource.G1GC);
        evaluate(generatedEvents, ZGC, EventSource.ZGC);
        evaluate(generatedEvents, SHENANDOAH, EventSource.SHENANDOAH);
        evaluate(generatedEvents, TENURING_DISTRIBUTION, SURVIVOR);
        return generatedEvents;

    }
}
