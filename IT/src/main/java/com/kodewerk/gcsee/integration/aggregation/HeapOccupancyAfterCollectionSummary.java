package com.kodewerk.gcsee.integration.aggregation;

import com.kodewerk.gcsee.aggregator.Collates;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.integration.collections.XYDataSet;
import com.kodewerk.gcsee.time.DateTimeStamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Collates(HeapOccupancyAfterCollectionAggregator.class)
public class HeapOccupancyAfterCollectionSummary extends HeapOccupancyAfterCollectionAggregation {

    private final Map<GarbageCollectionTypes, XYDataSet> aggregations = new ConcurrentHashMap<>();

    public void addDataPoint(GarbageCollectionTypes gcType, DateTimeStamp timeStamp, long heapOccupancy) {
        aggregations.computeIfAbsent(gcType, key -> new XYDataSet()).add(timeStamp.getTimeStamp(),heapOccupancy);
    }

    public Map<GarbageCollectionTypes, XYDataSet> get() {
        return aggregations;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return aggregations.isEmpty();
    }

    @Override
    public String toString() {
        return "Collected " + aggregations.size() + " different collection types";
    }
}
