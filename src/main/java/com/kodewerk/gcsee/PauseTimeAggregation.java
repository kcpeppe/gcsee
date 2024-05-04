package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.HashMap;

@Collates(PauseTimeAggregator.class)
public class PauseTimeAggregation extends Aggregation {

    private HashMap<GarbageCollectionTypes,XYSeries> pauseTimes = new HashMap<>();

    public void recordPauseTime(GarbageCollectionTypes gcType, double timeStamp, double heapOccupancyAfterCollection) {
        XYSeries series = pauseTimes.get(gcType);
        if (series == null) {
            series = new XYSeries(gcType.getLabel());
            pauseTimes.put(gcType,series);
        }
        series.add(timeStamp,heapOccupancyAfterCollection);
    }

    public XYSeriesCollection getPauseTimes() {
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        pauseTimes.keySet().
                stream().
                map(k -> pauseTimes.get(k)).
                forEach(s -> seriesCollection.addSeries(s));
        return seriesCollection;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return pauseTimes.isEmpty();
    }
}
