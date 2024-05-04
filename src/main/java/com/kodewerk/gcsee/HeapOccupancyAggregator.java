package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;

@Aggregates({EventSource.GENERATIONAL,EventSource.G1GC})
public class HeapOccupancyAggregator extends Aggregator<HeapOccupancyAggregation> {
    public HeapOccupancyAggregator(HeapOccupancyAggregation aggregation) {
        super(aggregation);
        register(G1GCPauseEvent.class, this::recordHeapOccupancy);
        register(GenerationalGCPauseEvent.class, this::recordHeapOccupancy);
    }

    private void recordHeapOccupancy(G1GCPauseEvent event) {
        aggregation().recordHeapOccupancyAfterCollection(event.getGarbageCollectionType(),
                event.getDateTimeStamp().getTimeStamp(),
                event.getHeap().getOccupancyAfterCollection());
    }

    private void recordHeapOccupancy(GenerationalGCPauseEvent event) {
        aggregation().recordHeapOccupancyAfterCollection(event.getGarbageCollectionType(),
                event.getDateTimeStamp().getTimeStamp(),
                event.getHeap().getOccupancyAfterCollection());
    }
}
