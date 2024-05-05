package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.CMSConcurrentEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;

@Aggregates({EventSource.GENERATIONAL,EventSource.G1GC})
public class TableDataAggregator extends Aggregator<TableDataAggregation> {

    public TableDataAggregator(TableDataAggregation aggregation) {
        super(aggregation);
        register(G1GCPauseEvent.class, this::record);
        register(G1GCConcurrentEvent.class,this::record);
        register(GenerationalGCPauseEvent.class, this::record);
        register(CMSConcurrentEvent.class, this::record);
    }

    public void record(G1GCPauseEvent event) {
        aggregation().recordPause(
                event.getDateTimeStamp().getTimeStamp(),
                event.getGarbageCollectionType(),
                event.getDuration()
        );
    }

    public void record(GenerationalGCPauseEvent event) {
        aggregation().recordPause(
                event.getDateTimeStamp().getTimeStamp(),
                event.getGarbageCollectionType(),
                event.getDuration()
        );
    }

    public void record(G1GCConcurrentEvent event) {
        aggregation().recordConcurrent(
                event.getDateTimeStamp().getTimeStamp(),
                event.getGarbageCollectionType(),
                event.getDuration()
        );
    }

    public void record(CMSConcurrentEvent event) {
        aggregation().recordConcurrent(
                event.getDateTimeStamp().getTimeStamp(),
                event.getGarbageCollectionType(),
                event.getDuration()
        );
    }
}
