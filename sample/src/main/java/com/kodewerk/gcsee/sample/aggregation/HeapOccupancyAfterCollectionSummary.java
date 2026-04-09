package com.kodewerk.gcsee.sample.aggregation;

import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.sample.collections.XYDataSet;
import com.kodewerk.gcsee.time.DateTimeStamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeapOccupancyAfterCollectionSummary extends HeapOccupancyAfterCollectionAggregation {

    private final Map<GarbageCollectionTypes, XYDataSet> aggregations = new ConcurrentHashMap<>();

    public HeapOccupancyAfterCollectionSummary() {}

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
