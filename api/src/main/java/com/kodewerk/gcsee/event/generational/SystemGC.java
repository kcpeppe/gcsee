// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class SystemGC extends FullGC {

    public SystemGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public SystemGC(DateTimeStamp timeStamp, GCCause cause, double duration) {
        this(timeStamp, GarbageCollectionTypes.SystemGC, cause, duration);
    }

    public SystemGC(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GarbageCollectionTypes.SystemGC, GCCause.JAVA_LANG_SYSTEM, duration);
    }
}
