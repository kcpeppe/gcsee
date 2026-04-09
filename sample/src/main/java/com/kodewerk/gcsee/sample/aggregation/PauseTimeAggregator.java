package com.kodewerk.gcsee.sample.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.g1gc.G1GCConcurrentEvent;
import com.kodewerk.gcsee.event.g1gc.G1GCPauseEvent;

/**
 * An Aggregator that extracts pause time.
 */
@Aggregates({EventSource.G1GC})
public class PauseTimeAggregator extends Aggregator<PauseTimeAggregation> {

    public PauseTimeAggregator(PauseTimeAggregation aggregation) {
        super(aggregation);
        register(G1GCPauseEvent.class, this::process);
    }

    private void process(G1GCPauseEvent event) {
        aggregation().recordPauseDuration(event.getDuration());
    }
}
