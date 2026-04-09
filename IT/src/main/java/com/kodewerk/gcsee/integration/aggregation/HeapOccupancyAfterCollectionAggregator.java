package com.kodewerk.gcsee.integration.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.g1gc.G1GCPauseEvent;
import com.kodewerk.gcsee.event.generational.GenerationalGCPauseEvent;
import com.kodewerk.gcsee.event.shenandoah.ShenandoahCycle;
import com.kodewerk.gcsee.event.zgc.ZGCFullCollection;
import com.kodewerk.gcsee.event.zgc.ZGCOldCollection;
import com.kodewerk.gcsee.event.zgc.ZGCYoungCollection;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
public class HeapOccupancyAfterCollectionAggregator extends Aggregator<HeapOccupancyAfterCollectionAggregation> {

    public HeapOccupancyAfterCollectionAggregator(HeapOccupancyAfterCollectionAggregation results) {
        super(results);
        register(GenerationalGCPauseEvent.class, this::extractHeapOccupancy);
        register(G1GCPauseEvent.class, this::extractHeapOccupancy);
        register(ZGCFullCollection.class, this::extractHeapOccupancy);
        register(ZGCOldCollection.class, this::extractHeapOccupancy);
        register(ZGCYoungCollection.class, this::extractHeapOccupancy);
        register(ShenandoahCycle.class,this::extractHeapOccupancy);
    }

    private void extractHeapOccupancy(ZGCYoungCollection event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(ZGCOldCollection event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(GenerationalGCPauseEvent event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());
    }

    private void extractHeapOccupancy(G1GCPauseEvent event) {
        // JDK 8.0 Remark doesn't report on heap occupancies.
        if ( event.getHeap() != null)
            aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getHeap().getOccupancyAfterCollection());
    }

    private void extractHeapOccupancy(ZGCFullCollection event) {
        aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getMemorySummary().getOccupancyAfter());
    }

    private void extractHeapOccupancy(ShenandoahCycle event) {
        //aggregation().addDataPoint(event.getGarbageCollectionType(), event.getDateTimeStamp(), event.getOccupancy());
    }
}

