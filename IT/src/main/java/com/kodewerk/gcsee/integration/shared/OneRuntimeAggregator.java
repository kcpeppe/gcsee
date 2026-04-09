package com.kodewerk.gcsee.integration.shared;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.SHENANDOAH,EventSource.ZGC})
public class OneRuntimeAggregator extends Aggregator<OneRuntimeReport> {
    /**
     * Subclass only.
     *
     * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
     */
    public OneRuntimeAggregator(OneRuntimeReport aggregation) {
        super(aggregation);
    }
}