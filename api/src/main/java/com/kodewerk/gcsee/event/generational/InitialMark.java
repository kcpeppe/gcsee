// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.event.MemoryPoolSummary;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class InitialMark extends CMSPauseEvent {

    public InitialMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.InitialMark, cause, duration);
    }

    public InitialMark(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public void add(MemoryPoolSummary tenured, MemoryPoolSummary heap) {
        this.add(heap.minus(tenured), tenured, heap);
    }

}
