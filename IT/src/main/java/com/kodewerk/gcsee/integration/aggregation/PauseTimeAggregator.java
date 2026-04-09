package com.kodewerk.gcsee.integration.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.g1gc.G1RealPause;
import com.kodewerk.gcsee.event.generational.GenerationalGCPauseEvent;

/**
 * An Aggregator that extracts pause time.
 */
@Aggregates({EventSource.G1GC, EventSource.GENERATIONAL})
public class PauseTimeAggregator extends Aggregator<PauseTimeAggregation> {

    public PauseTimeAggregator(PauseTimeAggregation aggregation) {
        super(aggregation);
        register(G1RealPause.class, this::process);
        register(GenerationalGCPauseEvent.class, this::publish);
    }

    private void publish(GenerationalGCPauseEvent event) {
        aggregation().recordPauseDuration(event.getDuration());
    }

    private void process(G1RealPause event) {
        aggregation().recordPauseDuration(event.getDuration());
    }
}
