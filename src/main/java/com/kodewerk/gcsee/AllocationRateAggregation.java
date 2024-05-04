package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@Collates(AllocationRateAggregator.class)
public class AllocationRateAggregation extends Aggregation {

    private XYSeries allocationRates = new XYSeries("Allocation Rates");
    private long lastHeapOccupancyAfterGC = 0L;
    private double endOfLastCollectionTime = 0.0d;

    public void recordHeapOccupancies(double startOfCollection, long heapOccupancyBeforeCollection, long heapOccupancyAfterCollection, double duration) {
        long allocated = heapOccupancyBeforeCollection - lastHeapOccupancyAfterGC;
        double runtime = startOfCollection - endOfLastCollectionTime;
        double allocationRate = (( runtime > 0.0d) && (allocated > 0L)) ? ((double)allocated) / runtime : 0.0d;
        allocationRates.add(startOfCollection,allocationRate);
        lastHeapOccupancyAfterGC = heapOccupancyAfterCollection;
        endOfLastCollectionTime = startOfCollection + duration;
    }

    public XYSeriesCollection getAllocationRates() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(allocationRates);
        return seriesCollection;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return allocationRates.isEmpty();
    }
}
