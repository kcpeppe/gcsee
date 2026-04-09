// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.generational;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class CMSRemark extends CMSPauseEvent {

    public CMSRemark(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GCCause.CMS_FINAL_REMARK, duration);
    }

    public CMSRemark(DateTimeStamp timeStamp, GCCause gcCause, double duration) {
        super(timeStamp, GarbageCollectionTypes.Remark, gcCause, duration);
    }


}
