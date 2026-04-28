// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.jvm;

import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Synthesised by the parser at end-of-log. Carries the two facts about the
 * run that downstream aggregators need: when the JVM that produced the log
 * started (best-effort estimate from {@link com.kodewerk.gcsee.jvm.Diary})
 * and how long it ran for. Both are final — JVMTermination is the canonical
 * carrier of these facts and they do not change once published.
 */
public class JVMTermination extends JVMEvent {

    private final DateTimeStamp startTime;
    private final double runtime;

    /**
     * estimatedTimeOfJVMTermination is the time the JVM terminated or the last
     * JVMEvent was seen. For the last JVMEvent, any pause time will have been
     * added to timeStamp. The duration normally means the duration of the
     * event. In this case duration doesn't refer to the time the JVM took to
     * shutdown but instead refers to the total running time of the JVM. This
     * is a recognized change in the definition of duration.
     *
     * @param estimatedTimeOfJVMTermination time of JVM termination message or
     *                                      end of last event seen
     * @param startTime estimated JVM start time, computed by
     *                  {@link com.kodewerk.gcsee.jvm.Diary#getEstimatedStartTime()}
     *                  during the diarizer pre-pass.
     */
    public JVMTermination(DateTimeStamp estimatedTimeOfJVMTermination, DateTimeStamp startTime) {
        super(estimatedTimeOfJVMTermination, 0.0d);
        this.startTime = startTime;
        this.runtime = computeRuntime(estimatedTimeOfJVMTermination, startTime);
    }

    public DateTimeStamp getTimeOfTerminationEvent() {
        return super.getDateTimeStamp();
    }

    /**
     * @return the JVM start time as estimated by the diarizer.
     */
    public DateTimeStamp getStartTime() {
        return startTime;
    }

    /**
     * @return wall-clock runtime of the JVM in decimal seconds:
     *         {@code timeOfTermination − startTime}.
     */
    public double getRuntime() {
        return runtime;
    }

    private static double computeRuntime(DateTimeStamp end, DateTimeStamp start) {
        if (end == null || start == null) {
            return 0.0d;
        }
        if (!end.hasTimeStamp() || !start.hasTimeStamp()) {
            return 0.0d;
        }
        double runtime = end.minus(start);
        if (Double.isNaN(runtime) || runtime < 0.0d) {
            return 0.0d;
        }
        return runtime;
    }

}
