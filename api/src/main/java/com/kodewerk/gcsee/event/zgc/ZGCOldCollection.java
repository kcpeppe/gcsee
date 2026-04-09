package com.kodewerk.gcsee.event.zgc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public class ZGCOldCollection extends ZGCCollection {
    public ZGCOldCollection(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public ZGCOldCollection(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public ZGCOldCollection(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public ZGCOldCollection(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }
}
