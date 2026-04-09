// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class DefNew extends GenerationalGCPauseEvent {

    public DefNew(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.DefNew, cause, duration);
    }
}
