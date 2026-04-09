// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Concurrent phase
 */

public class G1ConcurrentCleanup extends G1GCConcurrentEvent {

    /**
     * @param timeStamp time of the event
     * @param duration duration of the event
     */
    public G1ConcurrentCleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentCleanup, GCCause.GCCAUSE_NOT_SET, duration);
    }

    /**
     * @param timeStamp time of the event
     * @param cause reason to trigger the event
     * @param duration duration of the event
     */
    public G1ConcurrentCleanup(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCConcurrentCleanup, cause, duration);
    }

}
