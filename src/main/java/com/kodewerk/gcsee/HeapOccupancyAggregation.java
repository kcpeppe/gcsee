package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import java.util.HashMap;

@Collates(HeapOccupancyAggregator.class)
public class HeapOccupancyAggregation extends Aggregation {

    private HashMap<GarbageCollectionTypes,XYSeries> heapOccupancyAfterGC = new HashMap<>();
    private XYSeries heapSize;
    private Units scale = Units.KB;

    {
        heapSize = new XYSeries("Heap Size");
    }

    public void recordHeapOccupancyAfterCollection(GarbageCollectionTypes gcType, double timeStamp, long heapSizeAfterGC, long heapOccupancyAfterCollection) {
        XYSeries series = heapOccupancyAfterGC.get(gcType);
        if (series == null) {
            series = new XYSeries(gcType.getLabel());
            heapOccupancyAfterGC.put(gcType,series);
        }
        series.add(timeStamp,(double) heapOccupancyAfterCollection);
        heapSize.add(timeStamp, (double) heapSizeAfterGC);
    }

    public Units getScale() {
        return scale;
    }

    public XYSeriesCollection getHeapOccupancyAfterCollection() {
        scale = Units.KB;
        scale = Units.scaleFromKB(heapSize.getMaxY());
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(scale(heapSize));
        heapOccupancyAfterGC.keySet().
                stream().
                map(k -> heapOccupancyAfterGC.get(k)).
                map(s -> scale(s)).
                forEach(s -> seriesCollection.addSeries(s));
        return seriesCollection;
    }

    private XYSeries scale(XYSeries series) {
        XYSeries scaledSeries;
        scale = Units.scaleFromKB(series.getMaxY());
        double scaleFactor = switch (scale) {
            case KB -> 1.0d;
            case MB -> 1024.0d;
            case GB -> 1048576.0d;
        };

        if (scale == Units.KB)
            scaledSeries = series;
        else {
            scaledSeries = new XYSeries(series.getKey());
            for (int i = 0; i < series.getItemCount(); i++) {
                XYDataItem point = series.getDataItem(i);
                scaledSeries.add(point.getX(), point.getY().doubleValue() / scaleFactor);
            }
        }
        return scaledSeries;
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
