// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class G1ConcurrentMarkResetForOverflow extends G1GCConcurrentEvent {

    public G1ConcurrentMarkResetForOverflow(DateTimeStamp timeStamp) {
        super(timeStamp, GarbageCollectionTypes.G1ConcurrentMarkResetForOverflow, GCCause.CONCURRENT_MARK_STACK_OVERFLOW, 0.0d);
    }

}
