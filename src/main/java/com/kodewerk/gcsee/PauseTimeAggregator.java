package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;

@Aggregates({EventSource.GENERATIONAL,EventSource.G1GC})
public class PauseTimeAggregator extends Aggregator<PauseTimeAggregation> {
    public PauseTimeAggregator(PauseTimeAggregation aggregation) {
        super(aggregation);
        register(G1GCPauseEvent.class, this::recordPauseTime);
        register(GenerationalGCPauseEvent.class, this::recordPauseTime);
    }

    private void recordPauseTime(G1GCPauseEvent event) {
        aggregation().recordPauseTime(event.getGarbageCollectionType(),
                event.getDateTimeStamp().getTimeStamp(),
                event.getDuration());
    }

    private void recordPauseTime(GenerationalGCPauseEvent event) {
        aggregation().recordPauseTime(event.getGarbageCollectionType(),
                event.getDateTimeStamp().getTimeStamp(),
                event.getDuration());
    }
}
