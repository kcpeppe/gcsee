package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Collates(TableDataAggregator.class)
public class TableDataAggregation extends Aggregation {

    private double logDuration = 0.0d;

    // GC overhead
    double totalPauseTime = 0.0d;
    double totalConcurrentTime = 0.0d;
    // GC cycle interval for pause events
    int totalNumberOfPauseEvents = 0;
    int totalNumberOfConcurrentEvents = 0;
    // GC cycle interval for
    // GC type - count - total duration
    HashMap<GarbageCollectionTypes,Integer> gcCounts = new HashMap<>();
    HashMap<GarbageCollectionTypes,Double> gcPauseDurations = new HashMap<>();
    HashMap<GarbageCollectionTypes,Double> gcConcurrentDurations = new HashMap<>();

    public void recordPause(double timeOfGCCycle, GarbageCollectionTypes gcType, double duration) {
        logDuration = timeOfGCCycle + (duration / 1000.0d);
        totalPauseTime += duration;
        totalNumberOfPauseEvents++;
        int count;
        count = (gcCounts.containsKey(gcType)) ? gcCounts.get(gcType) : 0;
        gcCounts.put(gcType,++count);
        double pauseTimeRunningTotal;
        pauseTimeRunningTotal = (gcPauseDurations.containsKey(gcType)) ? gcPauseDurations.get(gcType) : 0.0d;
        gcPauseDurations.put(gcType,pauseTimeRunningTotal + duration);
    }

    public void recordConcurrent(double timeOfGCCycle, GarbageCollectionTypes gcType, double duration) {
        logDuration = timeOfGCCycle + (duration / 1000.0d);
        totalConcurrentTime += duration;
        totalNumberOfConcurrentEvents++;
        int count;
        count = (gcCounts.containsKey(gcType)) ? gcCounts.get(gcType) : 0;
        gcCounts.put(gcType,++count);
        double concurrentTimeRunningTotal;
        concurrentTimeRunningTotal = (gcConcurrentDurations.containsKey(gcType)) ? gcConcurrentDurations.get(gcType) : 0.0d;
        gcConcurrentDurations.put(gcType,concurrentTimeRunningTotal + duration);
    }

    public double logDuration() {
        return logDuration;
    }

    public double totalPauseTime() {
        return totalPauseTime;
    }

    public double totalConcurrentTime() {
        return totalConcurrentTime;
    }

    public int totalNumberOfPauseEvents() {
        return totalNumberOfPauseEvents;
    }

    public int totalNumberOfConcurrentEvents() {
        return totalNumberOfConcurrentEvents;
    }

    public HashMap<GarbageCollectionTypes,Integer> gcCounts() {
        return gcCounts;
    }

    public HashMap<GarbageCollectionTypes,Double> gcPauseDurations() {
        return gcPauseDurations;
    }

    public  HashMap<GarbageCollectionTypes,Double> gcConcurrentDurations() {
        return gcConcurrentDurations;
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return (totalNumberOfPauseEvents == 0) && (totalNumberOfConcurrentEvents == 0);
    }

    private double round( double value, double decimalPlaces) {
        double factor = 1.0d;
        for (int i = 0; i < decimalPlaces; i++)
            factor *= 10;
        return Math.round(value * factor) / factor;
    }

    /*
    Log Duration          : value
    Total Pause Time      : value
    Pause Interval        : value
    Total Concurrent Time : value
    Concurrent Interval   : value
    GC Overhead           : value
     */
    public String[][] summary() {
        String[][] tableData = new String[6][2];
        tableData[0][0] = "Log Duration";
        tableData[0][1] = Double.toString(round(logDuration, 3)) + " sec";
        tableData[1][0] = "Total Pause Time";
        tableData[1][1] = Double.toString(round(totalPauseTime, 3)) + " sec";
        tableData[2][0] = "Pause Interval";
        tableData[2][1] = Double.toString( round( logDuration / (double) totalNumberOfPauseEvents, 3)) + " sec";
        tableData[3][0] = "Total Concurrent Time";
        tableData[3][1] = Double.toString(round(totalConcurrentTime,3)) + " sec";
        tableData[4][0] = "Concurrent Interval";
        tableData[4][1] = Double.toString(round( logDuration / (double) totalNumberOfConcurrentEvents, 3)) + " sec";
        tableData[5][0] = "Application Throughput";
        tableData[5][1] = Double.toString(round((1.0d - (totalPauseTime / logDuration)) * 100.0d, 1)) + "%";
        return tableData;
    }

    /*
    gc type : count : concurrent time : pause time
     */
    public String[][] events() {
        String[][] summary = new String[gcCounts.size()][4];
        List<String[]> tableData = gcPauseDurations.keySet()
                .stream()
                .map(k -> {
                    String[] line = new String[4];
                    line[0] = k.getLabel();
                    line[1] = Integer.toString(gcCounts.get(k));
                    line[2] = "-";
                    line[3] = Double.toString(round(gcPauseDurations.get(k),3));
                    return line;
                })
                .collect(Collectors.toList());
        tableData.addAll(gcConcurrentDurations.keySet()
                .stream()
                .map(k -> {
                    String[] line = new String[4];
                    line[0] = k.getLabel();
                    line[1] = Integer.toString(gcCounts.get(k));
                    line[2] = Double.toString(round(gcConcurrentDurations.get(k),3));
                    line[3] = "-";
                    return line;
                })
                .collect(Collectors.toList()));
        return tableData.toArray(new String[0][0]);
    }
}
