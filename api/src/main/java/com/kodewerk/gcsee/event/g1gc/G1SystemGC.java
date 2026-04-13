// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class G1SystemGC extends G1FullGC {

    public G1SystemGC(DateTimeStamp timeStamp, double pauseTime) {
        super(timeStamp, GarbageCollectionTypes.SystemGC, GCCause.JAVA_LANG_SYSTEM, pauseTime);
    }
}
