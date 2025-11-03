// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Concurrent cleanup for next mark
 */

public class ConcurrentCleanupForNextMark extends G1GCConcurrentEvent {

    /**
     * @param timeStamp start of event
     * @param duration event duration
     */
    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, GCCause.UNKNOWN_GCCAUSE, duration);
    }

    /**
     *
     * @param timeStamp start of event
     * @param cause trigger for the event
     * @param duration event duration
     */
    public ConcurrentCleanupForNextMark(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentCleanupForNextMark, cause, duration);
    }

}
