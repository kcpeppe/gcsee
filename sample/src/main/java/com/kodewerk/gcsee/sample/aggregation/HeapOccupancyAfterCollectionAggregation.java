package com.kodewerk.gcsee.sample.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregation;
import com.kodewerk.gcsee.aggregator.Collates;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

@Collates(HeapOccupancyAfterCollection.class)
public abstract class HeapOccupancyAfterCollectionAggregation extends Aggregation {

    abstract public void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy);

}
