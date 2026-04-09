package com.kodewerk.gcsee.sample.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregation;
import com.kodewerk.gcsee.aggregator.Collates;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;

@Collates(CollectionCycleCountsAggregator.class)
public abstract class CollectionCycleCountsAggregation extends Aggregation {

    abstract public void count(GarbageCollectionTypes gcType);

}
