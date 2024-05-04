package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import java.util.HashMap;

@Collates(HeapOccupancyAggregator.class)
public class HeapOccupancyAggregation extends Aggregation {

    private HashMap<GarbageCollectionTypes,XYSeries> heapOccupancyAfterGC = new HashMap<>();

    public void recordHeapOccupancyAfterCollection(GarbageCollectionTypes gcType, double timeStamp, long heapOccupancyAfterCollection) {
        XYSeries series = heapOccupancyAfterGC.get(gcType);
        if (series == null) {
            series = new XYSeries(gcType.getLabel());
            heapOccupancyAfterGC.put(gcType,series);
        }
        series.add(timeStamp,(double) heapOccupancyAfterCollection);
    }

    public XYSeriesCollection getHeapOccupancyAfterCollection() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        heapOccupancyAfterGC.keySet().
                stream().
                map(k -> heapOccupancyAfterGC.get(k)).
                forEach(s -> seriesCollection.addSeries(s));
        return seriesCollection;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return heapOccupancyAfterGC.isEmpty();
    }
}
