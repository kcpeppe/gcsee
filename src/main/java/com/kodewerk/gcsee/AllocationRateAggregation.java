package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@Collates(AllocationRateAggregator.class)
public class AllocationRateAggregation extends Aggregation {

    private XYSeries allocationRates = new XYSeries("Allocation Rates");
    private Units units = Units.KB;
    private long lastHeapOccupancyAfterGC = 0L;
    private double endOfLastCollectionTime = 0.0d;

    public void setUnits(Units units) {
        this.units = units;
    }

    public void recordHeapOccupancies(double startOfCollection, long heapOccupancyBeforeCollection, long heapOccupancyAfterCollection, double duration) {
        long allocated = heapOccupancyBeforeCollection - lastHeapOccupancyAfterGC;
        double runtime = startOfCollection - endOfLastCollectionTime;
        double allocationRate = (( runtime > 0.0d) && (allocated > 0L)) ? ((double)allocated) / runtime : 0.0d;
        allocationRates.add(startOfCollection,allocationRate);
        lastHeapOccupancyAfterGC = heapOccupancyAfterCollection;
        endOfLastCollectionTime = startOfCollection + (duration/1000.0d);
    }

    public XYSeriesCollection getAllocationRates() {
        units = Units.scaleFromKB(allocationRates.getMaxY());
        allocationRates = switch (units) {
            case KB -> allocationRates;
            case MB -> scaleAllocationRates(1024.0d);
            case GB -> scaleAllocationRates(1048576.0d);
        };
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(allocationRates);
        return seriesCollection;
    }

    private XYSeries scaleAllocationRates(double scaleFactor) {
        XYSeries scaledSeries = new XYSeries("Allocation Rates");
        for (int i = 0; i < allocationRates.getItemCount(); i++) {
            XYDataItem point = allocationRates.getDataItem(i);
            scaledSeries.add( point.getX(), point.getY().doubleValue() / scaleFactor);
        }
        return scaledSeries;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return allocationRates.isEmpty();
    }

    public Units getUnits() {
        return units;
    }
}
