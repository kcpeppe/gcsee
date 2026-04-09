package com.kodewerk.gcsee.sample.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.g1gc.G1GCConcurrentEvent;
import com.kodewerk.gcsee.event.g1gc.G1GCPauseEvent;
import com.kodewerk.gcsee.event.generational.GenerationalGCPauseEvent;
import com.kodewerk.gcsee.event.shenandoah.ShenandoahCycle;
import com.kodewerk.gcsee.event.zgc.ZGCFullCollection;
import com.kodewerk.gcsee.event.zgc.ZGCOldCollection;
import com.kodewerk.gcsee.event.zgc.ZGCYoungCollection;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
public class CollectionCycleCountsAggregator extends Aggregator<CollectionCycleCountsAggregation> {

    public CollectionCycleCountsAggregator(CollectionCycleCountsAggregation results) {
        super(results);
        register(GenerationalGCPauseEvent.class, this::count);
        register(G1GCPauseEvent.class, this::count);
        register(G1GCConcurrentEvent.class, this::count);
        register(ZGCFullCollection.class, this::count);
        register(ZGCOldCollection.class, this::count);
        register(ZGCYoungCollection.class, this::count);
        register(ShenandoahCycle.class,this::count);
    }

    private void count(ZGCYoungCollection event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    private void count(ZGCOldCollection event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    private void count(ZGCFullCollection event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    private void count(ShenandoahCycle event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(G1GCPauseEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(G1GCConcurrentEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }

    public void count(GenerationalGCPauseEvent event) {
        aggregation().count(event.getGarbageCollectionType());
    }
}

