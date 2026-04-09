package com.kodewerk.gcsee.integration.shared;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;

@Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.SHENANDOAH,EventSource.ZGC})
public class TwoRuntimeAggregator extends Aggregator<TwoRuntimeReport> {
    /**
     * Subclass only.
     *
     * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
     */
    public TwoRuntimeAggregator(TwoRuntimeReport aggregation) {
        super(aggregation);
    }
}
