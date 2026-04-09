// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GCEvent;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public abstract class G1GCEvent extends GCEvent {

    public G1GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public G1GCEvent(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    public G1GCEvent(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.Unknown, cause, duration);
    }

    public G1GCEvent(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        this(timeStamp, gcType, GCCause.UNKNOWN_GCCAUSE, duration);
    }
}
