// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.parser.jvm;

import com.kodewerk.gcsee.jvm.Diarizer;
import com.kodewerk.gcsee.jvm.Diary;
import com.kodewerk.gcsee.parser.unified.UnifiedLoggingLevel;
import com.kodewerk.gcsee.time.DateTimeStamp;

import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kodewerk.gcsee.jvm.SupportedFlags.GC_CAUSE;
import static com.kodewerk.gcsee.jvm.SupportedFlags.*;
import static com.kodewerk.gcsee.parser.unified.ShenandoahPatterns.SHENANDOAH_TAG;
import static com.kodewerk.gcsee.parser.unified.UnifiedG1GCPatterns.G1_COLLECTION;
import static com.kodewerk.gcsee.parser.unified.UnifiedG1GCPatterns.G1_TAG;
import static com.kodewerk.gcsee.parser.unified.UnifiedGenerationalPatterns.*;
import static com.kodewerk.gcsee.parser.unified.UnifiedPatterns.CPU_BREAKOUT;
import static com.kodewerk.gcsee.parser.unified.ZGCPatterns.CYCLE_START;
import static com.kodewerk.gcsee.parser.unified.ZGCPatterns.ZGC_TAG;

//ShenandoahPatterns, ZGCPatterns, UnifiedG1GCPatterns, UnifiedGenerationalPatterns,
public class UnifiedDiarizer implements Diarizer {

    private static final int CYCLES_TO_EXAMINE_BEFORE_GIVING_UP = 10;
    private static final int CYCLES_TO_EXAMINE_FOR_SAFEPOINT = 2;

    /** Matches the unified-log GC id token, e.g. {@code GC(0)}, {@code GC(42)}. */
    private static final Pattern GC_ID_PATTERN = Pattern.compile("GC\\((\\d+)\\)");

    private int lineCount = MAXIMUM_LINES_TO_EXAMINE;

    private final Diary diary;
    private final TreeSet<String> tagsAndLevels = new TreeSet<>();
    private int stopTheWorldEvents = 0;

    {
        diary = new Diary();
        diary.setTrue(UNIFIED_LOGGING);
        diary.setFalse(ICMS, PRE_JDK70_40, JDK70, PRE_JDK70_40, JDK80, MAX_TENURING_THRESHOLD_VIOLATION);

    }

    public UnifiedDiarizer() {}

    public Diary getDiary() {
        fillInKnowns();
        diary.finalise();
        return diary;
    }


    @Override
    public boolean isUnified() {
        return true;
    }

    @Override
    public String getCommandLine() {
        // TODO #147 extract command line from the log file
        return "";
    }

    @Override
    public int getMaxTenuringThreshold() {
        return 0;
    }

    @Override
    public boolean hasJVMEvents() {
        return diary.isApplicationStoppedTime() ||
                diary.isApplicationRunningTime() ||
                diary.isTLABData();
    }

    private void fillInKnowns() {
        diary.setFalse(ADAPTIVE_SIZING);
    }

    @Override
    public boolean completed() {
        return diary.isComplete() || lineCount < 1;
    }

    @Override
    public boolean diarize(String line) {
        if ( ! this.completed()) {
            if (!line.startsWith("["))
                return false;
            lineCount--;
            extractDecorators(line);
            detectZeroGcid(line);
            if (!diary.isCollectorKnown())
                discoverCollector(line);
            if (!diary.isDetailsKnown())
                discoverDetails(line);
            if (!diary.isJVMEventsKnown())
                discoverJVMEvents(line);
            if ((CPU_BREAKOUT.parse(line) != null) || line.contains("gc,start"))
                stopTheWorldEvents++;
        }
        return this.completed();
    }

    /**
     * Inspect a unified-log line for a {@code GC(N)} token and use the first
     * one we find to settle the {@link com.kodewerk.gcsee.jvm.SupportedFlags#ZERO_GCID}
     * verdict for the log: {@code GC(0)} ⇒ TRUE (log starts at the JVM's first
     * collection), {@code GC(N>0)} ⇒ FALSE (rotated tail). Lines without a
     * {@code GC(N)} token are skipped. No-op once the verdict is known.
     */
    private void detectZeroGcid(String line) {
        if (diary.isZeroGCIDKnown()) {
            return;
        }
        Matcher m = GC_ID_PATTERN.matcher(line);
        if (m.find()) {
            try {
                int id = Integer.parseInt(m.group(1));
                if (id == 0) {
                    diary.setTrue(ZERO_GCID);
                } else {
                    diary.setFalse(ZERO_GCID);
                }
            } catch (NumberFormatException ignored) {
                // Pattern guarantees digits-only; this branch is unreachable
                // but keeps a malformed match from blowing up the pre-pass.
            }
        }
    }

    /**
     * Extract decorators (from a GC log line tag) and set the corresponding diary flags accordingly
     *
     * @param line GC log line
     */
    private void extractDecorators(String line) {
        Decorators decorators = new Decorators(line);
        diary.recordEventTimestamp(decorators.getDateTimeStamp());
        extractTagsAndLevels(decorators);
        // -Xlog:gc*,gc+ref=debug,gc+phases=debug,gc+age=trace,safepoint
        if (decorators.getLogLevel().isPresent()) {
            UnifiedLoggingLevel logLevel = decorators.getLogLevel().get();
            if (decorators.tagsContain("gc,age"))
                diary.setTrue(TENURING_DISTRIBUTION);
            else if (decorators.tagsContain("ref") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                diary.setTrue(PRINT_REFERENCE_GC);
            else if (decorators.tagsContain("gc,phases") && logLevel.isGreaterThanOrEqualTo(UnifiedLoggingLevel.debug))
                diary.setTrue(GC_DETAILS);
            else if ( decorators.tagsContain("gc,ergo"))
                diary.setTrue(ADAPTIVE_SIZING);
            else if (decorators.tagsContain("gc,cpu"))
            	diary.setTrue(PRINT_CPU_TIMES);
            
            if (decorators.tagsContain("safepoint"))
                diary.setTrue(APPLICATION_STOPPED_TIME, APPLICATION_CONCURRENT_TIME);

            if (diary.isZGC()) {
                if (decorators.tagsContain("task"))
                    diary.setTrue(GC_DETAILS);
                else if (decorators.tagsContain("heap"))
                    diary.setTrue(PRINT_HEAP_AT_GC);
                else if (decorators.tagsContain("tlab"))
                    diary.setTrue(TLAB_DATA);
                else if (decorators.tagsContain("gc,start") && line.contains("Garbage Collection ("))
                    diary.setTrue(GC_CAUSE);
                else if (decorators.tagsContain("gc,heap")) {
                    if (line.contains("Heap before GC"))
                        diary.setTrue(PRINT_HEAP_AT_GC);
                    diary.setTrue(GC_DETAILS);
                } else if (decorators.tagsContain("gc,ref"))
                    diary.setTrue(PRINT_REFERENCE_GC);
                else if (decorators.tagsContain("gc,heap") && decorators.getLogLevel().get() == UnifiedLoggingLevel.debug)
                    diary.setTrue(PRINT_HEAP_AT_GC);
            } else if (diary.isShenandoah()) {
                if (decorators.tagsContain("gc,task") || decorators.tagsContain("gc,start"))
                    diary.setTrue(GC_DETAILS);
                else if (decorators.tagsContain("gc,ergo"))
                    diary.setTrue(ADAPTIVE_SIZING);
                else if (decorators.tagsContain("gc") && line.contains("Trigger"))
                    diary.setTrue(GC_CAUSE);
            }
        }
    }

    private void extractTagsAndLevels(Decorators decorators) {
        tagsAndLevels.add(decorators.getLogLevel() + ":" + decorators.getTags());
    }

    /*
    G1 flags
        ADAPTIVE_SIZING,RSET_STATS,PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS
        todo: test for log segments that are missing the header lines that will contain the "use" keyword
        ZGC - x
        Shenandoah - x
        G1 - done
        CMS - x
        Parallel - x
        Serial - x
     */
    private void discoverCollector(String line) {

        if (CYCLE_START.parse(line) != null) {
            String cycleType = CYCLE_START.parse(line).getGroup(2);
            diary.setTrue(ZGC);
            if(cycleType.equals("Minor") || cycleType.equals("Major"))
                diary.setTrue(GENERATIONAL_ZGC);
            else
                diary.setFalse(GENERATIONAL_ZGC);
            diary.setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, G1GC, RSET_STATS, SHENANDOAH, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, TENURING_DISTRIBUTION, MAX_TENURING_THRESHOLD_VIOLATION, TLAB_DATA, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, ADAPTIVE_SIZING);
            return;
        }

        if ( SHENANDOAH_TAG.parse(line) != null) {
            diary.setTrue(SHENANDOAH);
            diary.setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, G1GC, RSET_STATS, ZGC, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, TENURING_DISTRIBUTION, MAX_TENURING_THRESHOLD_VIOLATION, TLAB_DATA, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, PRINT_HEAP_AT_GC, GENERATIONAL_ZGC);
            return;
        }

        if (G1_TAG.parse(line) != null || line.contains("G1 Evacuation Pause") || (line.contains("Humongous regions: "))) {
            diary.setTrue(G1GC);
            diary.setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, ZGC, SHENANDOAH, CMS_DEBUG_LEVEL_1, PRE_JDK70_40, JDK70, JDK80, TLAB_DATA, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, GENERATIONAL_ZGC);
            return;
        }

        if (CMS_TAG.parse(line) != null ||
                PARNEW_TAG.parse(line) != null ||
                line.contains("ParNew")) {
            diary.setTrue(PARNEW, CMS);
            diary.setFalse(DEFNEW, SERIAL, PARALLELGC, PARALLELOLDGC, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS, GENERATIONAL_ZGC);
            return;
        }

        if (PARALLEL_TAG.parse(line) != null ||
                line.contains("ParOldGen") ||
                line.contains("PSYoungGen")) {
            diary.setTrue(PARALLELGC, PARALLELOLDGC, GC_CAUSE);
            diary.setFalse(DEFNEW, SERIAL, PARNEW, CMS, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS, GENERATIONAL_ZGC);
            return;
        }

        if (SERIAL_TAG.parse(line) != null || line.contains("DefNew")) {
            diary.setTrue(DEFNEW, SERIAL, GC_CAUSE);
            diary.setFalse(PARALLELGC, PARALLELOLDGC, PARNEW, CMS, ICMS, CMS_DEBUG_LEVEL_1, G1GC, ZGC, SHENANDOAH, PRE_JDK70_40, JDK70, JDK80, RSET_STATS, GENERATIONAL_ZGC);
            return;
        }
    }

    /**
     *
     * @param line
     */
    private void discoverDetails(String line) {

        //todo: RSET_STATS for G1 not looked for...
        if (G1_COLLECTION.parse(line) != null) {
            diary.setTrue(GC_CAUSE);
        }

        else if (CYCLE_START.parse(line) != null) {
            diary.setTrue(GC_CAUSE);
        }

        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_BEFORE_GIVING_UP)
            diary.setFalse(ADAPTIVE_SIZING, GC_CAUSE, TLAB_DATA, PRINT_REFERENCE_GC, PRINT_PROMOTION_FAILURE, PRINT_FLS_STATISTICS, RSET_STATS, PRINT_HEAP_AT_GC);
    }

    private void discoverJVMEvents(String line) {
        if (stopTheWorldEvents > CYCLES_TO_EXAMINE_FOR_SAFEPOINT) {
            diary.setFalse(APPLICATION_STOPPED_TIME, APPLICATION_CONCURRENT_TIME);
        }
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return diary.getTimeOfFirstEvent();
    }

}