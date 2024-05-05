package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;

@Aggregates({EventSource.GENERATIONAL,EventSource.G1GC})
public class AllocationRateAggregator extends Aggregator<AllocationRateAggregation> {
    public AllocationRateAggregator(AllocationRateAggregation aggregation) {
        super(aggregation);
        register(G1GCPauseEvent.class, this::recordAllocationRate);
        register(GenerationalGCPauseEvent.class, this::recordAllocationRate);
        aggregation().setUnits(Units.KB);
    }

    private void recordAllocationRate(G1GCPauseEvent event) {
        aggregation().recordHeapOccupancies(event.getDateTimeStamp().getTimeStamp(),
                event.getHeap().getOccupancyBeforeCollection(),
                event.getHeap().getOccupancyAfterCollection(),
                event.getDuration());
    }

    private void recordAllocationRate(GenerationalGCPauseEvent event) {
        aggregation().recordHeapOccupancies(event.getDateTimeStamp().getTimeStamp(),
                event.getHeap().getOccupancyBeforeCollection(),
                event.getHeap().getOccupancyAfterCollection(),
                event.getDuration());
    }
}
