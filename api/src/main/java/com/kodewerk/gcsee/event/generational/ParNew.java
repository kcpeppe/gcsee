// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.event.TLABSummary;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class ParNew extends GenerationalGCPauseEvent {

    private TLABSummary tlabSummary = null;

    private int dutyCycle = -1;

    public ParNew(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        this(dateTimeStamp, GarbageCollectionTypes.ParNew, gcCause, pauseTime);
    }

    public ParNew(DateTimeStamp dateTimeStamp, GarbageCollectionTypes gcCollectionType, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, gcCollectionType, gcCause, pauseTime);
    }

    public void recordDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

    public void recordTLabSummary() {
        tlabSummary = new TLABSummary();
    }

    public TLABSummary getTlabSummary() {
        return tlabSummary;
    }

}
