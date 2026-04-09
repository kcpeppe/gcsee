package com.kodewerk.gcsee.integration.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregation;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

public abstract class HeapOccupancyAfterCollectionAggregation extends Aggregation {

    public abstract void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy);

}
