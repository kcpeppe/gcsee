// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public abstract class CMSConcurrentEvent extends GenerationalGCEvent implements CMSPhase {

    private double cpuTime;
    private double wallClockTime;

    protected CMSConcurrentEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration, double cpuTime, double wallClockTime) {
        super(timeStamp, type, cause, duration);
        this.cpuTime = cpuTime;
        this.wallClockTime = wallClockTime;
    }

    public double getCpuTime() {
        return cpuTime;
    }

    public double getWallClockTime() {
        return wallClockTime;
    }

}
