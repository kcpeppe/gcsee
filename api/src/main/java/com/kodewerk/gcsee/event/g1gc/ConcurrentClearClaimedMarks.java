// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Concurrent phase, Clear claimed marks
 */

public class ConcurrentClearClaimedMarks extends G1GCConcurrentEvent {

    /**
     *
     * @param timeStamp time of event
     * @param duration duration of event
     */
    public ConcurrentClearClaimedMarks(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentClearClaimedMarks, GCCause.UNKNOWN_GCCAUSE, duration);
    }


}
