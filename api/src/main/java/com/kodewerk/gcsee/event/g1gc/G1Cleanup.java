// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com..event.GCCause;
import com..event.GarbageCollectionTypes;
import com..time.DateTimeStamp;

/**
 * Pause phase
 */
public class G1Cleanup extends G1RealPause {

    /**
     * @param timeStamp time of the event
     * @param duration duration of the event
     */
    public G1Cleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCCleanup, GCCause.GCCAUSE_NOT_SET, duration);
    }

}
