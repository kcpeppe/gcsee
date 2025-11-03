// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Concurrent phase, complete cleanup at end of G1GC Concurrent cycle
 */

public class ConcurrentCompleteCleanup extends G1GCConcurrentEvent {

    /**
     * @param timeStamp time of event
     * @param duration duration of event
     */
    public ConcurrentCompleteCleanup(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCompleteCleanup, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     *
     * @param timeStamp time of event
     * @param cause reason for triggering this event
     * @param duration duration of event
     */
    public ConcurrentCompleteCleanup(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCompleteCleanup, cause, duration);
    }

}
