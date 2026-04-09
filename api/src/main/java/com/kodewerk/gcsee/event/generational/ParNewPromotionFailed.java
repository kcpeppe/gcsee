// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class ParNewPromotionFailed extends ParNew {

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, GarbageCollectionTypes.ParNewPromotionFailed, gcCause, pauseTime);
    }

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, double pauseTime) {
        this(dateTimeStamp, GCCause.PROMOTION_FAILED, pauseTime);
    }
}
