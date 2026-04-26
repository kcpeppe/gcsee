// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.aggregator;

import com.kodewerk.gcsee.event.jvm.JVMEvent;
import com.kodewerk.gcsee.jvm.JavaVirtualMachine;
import com.kodewerk.gcsee.online.statistics.WelfordVarianceCalculator;
import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * An {@code Aggregation} collates data from an {@link Aggregator} and may be thought of as a view
 * of the data. An {@code Aggregation} might collate data into a time series for plotting, or it might
 * summarize the data. Separating the capture of the data, which is the job of the {@code Aggregator}
 * from how the data is aggregated, which is the job of the {@code Aggregation}, allows for multiple
 * views of the data to be built for the same {@code Aggregator}.
 * <p>
 * The {@link Collates} annotation is used to indicate which {@code Aggregator} an {@code Aggregation}
 * works with. If an {@code Aggregation} does not have the {@code Collates} annotation, the {@code Aggregation}
 * will not be used.
 * <p>
 * An implementation of Aggregation must have a public, no-arg constructor.
 * <p>
 * Best practice for creating an {@code Aggregation} is to create an interface for the methods the
 * {@code Aggregator} will call.  When a GC log is analyzed, {@code JVMEvents} are captured by the
 * {@code Aggregators}. An {@code Aggregator} extracts data from the JVMEvent and calls the
 * {@code Aggregation} API to collate the data.
 * <p>
 * As an example, say one wants to record the pause times of full GCs. An {@code Aggregation}
 * could present an API that takes the date/time of the event, the cause of the full GC, and
 * the duration of the full GC. The {@code Aggregator} could capture G1FullGC events and
 * FullGC events. Which event is actually sent to the {@code Aggregator} depends on the
 * what kind of GC log file is being parsed.
 * <p>
 * The example FullGCAggregator is annotated with the {@code @Aggregates} annotation, giving
 * the G1GC and Generational as the event source. This lets GCSee know that this Aggregator
 * is capturing events from those sources. Notice also that the constructor of FullGCAggregator
 * registers the JVMEvent types that it is interested in, and gives the method to call for that
 * event type. Lastly, the process method extracts the data from the event and calls the
 * FullGCAggregation API.
 * <p>
 * The implementation of FullGCAggregation can collate the data however desired. MaxFullGCPauseTime is
 * just one example. Notice that the method to get the maximum pause time is defined in MaxFullGCPauseTime,
 * not in FullGCAggregation. This keeps the FullGCAggregation interface from imposing API that some
 * other view (some other Aggregation) of the data might not want or need.
 *
 * <pre><code>
 * {@literal @}Collates(FullGCAggregator.class)
 * public interface FullGCAggregation extends Aggregation {
 *      void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime);
 * }
 *
 * {@literal @}Aggregates({EventSource.G1GC, EventSource.Generational})
 * public class FullGCAggregator implements Aggregator{@literal <}FullGCAggregation{@literal >} {
 *
 *     public FullGCAggregator(FullGCAggregation aggregation) {
 *         super(aggregation);
 *         register(G1FullGC.class, this::process);
 *         register(FullGC.class, this::process);
 *     }
 *
 *      private void process(GCEvent event) {
 *          aggregation().recordFullGC(event.getDateTimeStamp(), event.getGCCause(), event.getDuration());
 *      }
 * }
 *
 * public class MaxFullGCPauseTime implements FullGCAggregation {
 *     Map{@literal <}GCCause, Double{@literal >} maxPauseTime = new HashMap();
 *
 *     public MaxFullGCPauseTime() {}
 *
 *     {@literal @}Override
 *     public void recordFullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
 *         maxPauseTime.compute(cause, (k, v) -{@literal >} (v == null) ? pauseTime : Math.max(v, pauseTime));
 *     }
 *
 *     public double getMaxPauseTime(GCCause cause) {
 *         return maxPauseTime.get(cause);
 *     }
 *
 *     {@literal @}Override
 *     public boolean hasWarning() { return false; }
 *
 *     {@literal @}Override
 *     public boolean isEmpty() { return maxPauseTime.isEmpty(); }
 * }
 * </code></pre>
 *
 * @see JavaVirtualMachine#getAggregation(Class)
 * @see Collates
 */
public abstract class Aggregation {

    /**
     * If the first line of the GC log is observed within this many seconds of
     * the JVM origin we treat the log as untruncated and report
     * {@link #estimatedStartTime()} as 0. Beyond this threshold the log is
     * presumed to have been rotated and the start time is back-extrapolated
     * from the first event we saw.
     */
    private static final double EFFECTIVE_LOG_START_EPSILON_SECONDS = 0.5;

    /**
     * Number of inter-event intervals we average when back-extrapolating the
     * JVM start time from the first observed event on a (presumed-truncated)
     * log. Five gives a stable mean without leaning on long-tail outliers.
     */
    private static final int FIRST_INTERVAL_SAMPLE_SIZE = 5;

    /** Time of the first line in the GC log, populated from the JVMTermination event. */
    private DateTimeStamp timeOfFirstEvent = null;
    /**
     * Timestamp on the JVMTermination event. Initialised to {@link DateTimeStamp#baseDate()}
     * (timestamp 0.0) which is the sentinel for "no JVMTermination has been published";
     * a real JVMTermination always has a positive uptime.
     */
    private DateTimeStamp timeOfTermination = DateTimeStamp.baseDate();
    private final WelfordVarianceCalculator varianceCalculator = new WelfordVarianceCalculator();
    private DateTimeStamp timeOfLastSeenEvent = null;

    /**
     * End-of-last-event uptime ({@code event.getDateTimeStamp() + event.getDuration()})
     * for every event delivered to the owning aggregator. Used to compute the runtime
     * when no JVMTermination event was published (truncated tail / live tailing).
     */
    private DateTimeStamp endOfLastReceivedEvent = null;

    /** Sum of the first {@link #FIRST_INTERVAL_SAMPLE_SIZE} true inter-event intervals. */
    private double sumOfFirstIntervals = 0.0;
    /** Number of intervals contributing to {@link #sumOfFirstIntervals}, capped at the sample size. */
    private int countOfFirstIntervals = 0;
    /** Previous event's timestamp, used to compute the next interval. Independent of the
     *  pre-existing {@code timeOfLastSeenEvent} (which feeds the legacy variance calculator). */
    private DateTimeStamp previousEventTimeStamp = null;

    /**
     * Constructor for the module SPI
     */
    protected Aggregation() {}

    /**
     * @param eventTime of first event seen
     */
    public void timeOfFirstEvent(DateTimeStamp eventTime) {
        this.timeOfFirstEvent = eventTime;
    }

    /**
     * @return time of first event seen
     */
    public DateTimeStamp timeOfFirstEvent() {
        return this.timeOfFirstEvent;
    }

    /**
     * Interface to record the time span of the log
     * Estimate based on information carried in the JVMTermination event.
     * @param eventTime - estimate start time of the log.
     */
    public void timeOfTerminationEvent(DateTimeStamp eventTime) {
        this.timeOfTermination = eventTime;
    }

    /**
     * @return the timestamp reported by the JVM termination record if present otherwise the end of the last event.
     */
    public DateTimeStamp timeOfTerminationEvent() {
        return this.timeOfTermination;
    }

    /**
     * Estimates the JVM start time from the available log data.
     * <p>
     * The first line of the GC log carries an uptime which is recorded on
     * {@link com.kodewerk.gcsee.jvm.Diary} during parsing and propagated here through
     * the JVMTermination event. Two cases:
     * <ul>
     *   <li>If that uptime is at most {@value #EFFECTIVE_LOG_START_EPSILON_SECONDS} seconds
     *       the log is treated as untruncated — the JVM started essentially at log time
     *       0 and {@link DateTimeStamp#baseDate()} is returned.</li>
     *   <li>Otherwise the log is presumed to have been rotated and the JVM was already
     *       running before we saw any log line. The start is back-extrapolated as
     *       {@code timeOfFirstEvent − meanInterval}, where {@code meanInterval} is the
     *       mean of the first {@value #FIRST_INTERVAL_SAMPLE_SIZE} inter-event intervals
     *       observed by the owning aggregator.</li>
     * </ul>
     * Falls back to {@code timeOfFirstEvent} when no intervals are available (e.g. an
     * aggregator that received only a single event), and to {@code baseDate()} when no
     * events were observed at all.
     *
     * @return The estimated JVM start time
     */
    public DateTimeStamp estimatedStartTime() {
        if (timeOfFirstEvent == null) {
            return DateTimeStamp.baseDate();
        }
        if (timeOfFirstEvent.hasTimeStamp()
                && timeOfFirstEvent.toSeconds() <= EFFECTIVE_LOG_START_EPSILON_SECONDS) {
            return DateTimeStamp.baseDate();
        }
        if (countOfFirstIntervals > 0) {
            double meanInterval = sumOfFirstIntervals / countOfFirstIntervals;
            DateTimeStamp estimate = timeOfFirstEvent.minus(meanInterval);
            // DateTimeStamp clamps negative timestamps to NaN; in that case the
            // back-extrapolation is unsupported and we fall back to first-event time.
            if (estimate.hasTimeStamp()) {
                return estimate;
            }
        }
        return timeOfFirstEvent;
    }

    /**
     * Estimated wall-clock runtime of the JVM that produced the log.
     * <p>
     * The end of the run is the timestamp on the JVMTermination event when one was
     * published; otherwise it is the time of the last observed event plus that event's
     * duration. The start of the run is {@link #estimatedStartTime()}.
     *
     * @return runtime in decimal seconds
     */
    public double estimatedRuntime() {
        return endOfLog().minus(estimatedStartTime());
    }

    /**
     * Authoritative end-of-log time used by {@link #estimatedRuntime()}. Internal —
     * the public {@link #timeOfTerminationEvent()} accessor is unchanged so external
     * callers see the raw termination timestamp (which is {@code baseDate()} when no
     * termination was published).
     */
    private DateTimeStamp endOfLog() {
        // A real JVMTermination always has a positive uptime; baseDate (0.0) is the
        // sentinel for "no JVMTermination event was published".
        if (timeOfTermination != null
                && timeOfTermination.hasTimeStamp()
                && timeOfTermination.toSeconds() > 0.0) {
            return timeOfTermination;
        }
        if (endOfLastReceivedEvent != null && endOfLastReceivedEvent.hasTimeStamp()) {
            return endOfLastReceivedEvent;
        }
        // Nothing observed — return the sentinel so estimatedRuntime collapses to 0.
        return timeOfTermination;
    }

    /**
     * Return true if the Aggregation contains a warning. For example, an Aggregation that
     * looks at GC Cause might return {@code true} if it finds a System.gc() call.
     * @return {@code true} if the Aggregation contains a warning.
     */
    abstract public boolean hasWarning();

    /**
     * Return {@code true} if there is no data in the Aggregation.
     * @return {@code true} if there is no data in the Aggregation.
     */
    abstract public boolean isEmpty();

    /**
     * Sort if a given Aggregator collates for this aggregation.
      * @return aggregator
     */
    public Class<? extends Aggregator<?>> collates() {
        return collates(getClass());
    }

    public void updateEventFrequency(JVMEvent event) {
        final DateTimeStamp dateTimeStamp = event.getDateTimeStamp();
        if (dateTimeStamp == null) {
            return;
        }

        // (1) Track the end of the last received event for the no-JVMTermination
        // fallback in endOfLog(). Uses JVMEvent.getDuration() which the framework
        // exposes uniformly for every event type.
        double duration = event.getDuration();
        if (Double.isNaN(duration) || duration < 0.0) {
            endOfLastReceivedEvent = dateTimeStamp;
        } else {
            endOfLastReceivedEvent = dateTimeStamp.add(duration);
        }

        // (2) Sample the first FIRST_INTERVAL_SAMPLE_SIZE true inter-event intervals;
        // their mean drives the start-time back-extrapolation in estimatedStartTime().
        if (previousEventTimeStamp != null
                && countOfFirstIntervals < FIRST_INTERVAL_SAMPLE_SIZE) {
            double interval = dateTimeStamp.minus(previousEventTimeStamp);
            if (interval >= 0.0 && !Double.isNaN(interval)) {
                sumOfFirstIntervals += interval;
                countOfFirstIntervals++;
            }
        }
        previousEventTimeStamp = dateTimeStamp;

        // (3) Pre-existing variance bookkeeping is retained for backward compatibility;
        // it is no longer consulted by estimatedStartTime() but other callers may rely on it.
        if (timeOfLastSeenEvent == null) {
            timeOfLastSeenEvent = dateTimeStamp;
            return;
        }
        double timeSpan = dateTimeStamp.minus(timeOfLastSeenEvent);
        varianceCalculator.update(timeSpan);
    }

    /**
     * Calculates the aggregator for this aggregation.
     * @param clazz this Aggregation
     * @return the Aggregator
     */
    private Class<? extends Aggregator<?>> collates(Class<?> clazz) {
        Class<? extends Aggregator<?>> target;
        if (clazz != null && clazz != Aggregation.class) {

            if (clazz.isAnnotationPresent(Collates.class)) {
                Collates collates = clazz.getAnnotation(Collates.class);
                return collates.value();
            }

            Class<?> superClass = clazz.getSuperclass();
            target = collates(superClass);
            if ( target != null)
                return target;

            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> iface : interfaces) {
                target = collates(iface);
                if (target != null)
                    return target;
            }
        }
        return null;
    }
}
