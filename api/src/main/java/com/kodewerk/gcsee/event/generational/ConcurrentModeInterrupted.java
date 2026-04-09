// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class ConcurrentModeInterrupted extends FullGC implements CMSPhase {

    public ConcurrentModeInterrupted(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public ConcurrentModeInterrupted(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.ConcurrentModeInterrupted, cause, duration);
    }
}
