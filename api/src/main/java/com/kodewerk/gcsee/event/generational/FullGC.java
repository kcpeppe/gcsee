// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class FullGC extends GenerationalGCPauseEvent {

    private int dutyCycle = -1;

    public FullGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public FullGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.FullGC, cause, duration);
    }

    public void recordDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

}
