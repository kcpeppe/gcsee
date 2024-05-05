package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GCSee extends JFrame {
    public static void main( String[] args ) {
        GCSee window = new GCSee();
    }

    private static void setNimbusLookAndFeel() {
        String lookAndFeel = "Nimbus";
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeel.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException  e) {
            e.printStackTrace();
        }
    }

    private final JMenuBar menuBar;
    private final JTabbedPane pane = new JTabbedPane();

    public GCSee() {
        setNimbusLookAndFeel();
        setTitle("GCSee");
        menuBar = new GCSeeMenuBar(this);
        setJMenuBar(menuBar);
        super.setContentPane(pane);
        super.setSize(800, 600);
        super.setContentPane(pane);
        super.setLocationRelativeTo(null);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setVisible(true);
    }

    public void exit(int level) {
        System.exit(level);
    }

    void load(File selectedFile) {
        System.out.println("loading: " + selectedFile.getPath());
        try {
            setTitle(selectedFile.getPath());
            GCLogFile logFile = new SingleGCLogFile(selectedFile.toPath());
            ExecutorService pool = Executors.newSingleThreadExecutor();
            pool.execute(() -> {
                GCToolKit toolKit = new GCToolKit();
                toolKit.loadAggregation(new HeapOccupancyAggregation());
                toolKit.loadAggregation(new PauseTimeAggregation());
                toolKit.loadAggregation(new AllocationRateAggregation());
                JavaVirtualMachine jvm = null;
                try {
                    jvm = toolKit.analyze(logFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable t) {}
                buildSummaryTable(jvm);
                buildHeapAfterGC(jvm);
                buildPauseTime(jvm);
                buildAllocationRate(jvm);
            });
        } catch(Throwable t) {
            JOptionPane.showMessageDialog(this,"Error processing log", selectedFile.toPath().toString(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildSummaryTable(JavaVirtualMachine jvm) {

    }

    private void buildHeapAfterGC(JavaVirtualMachine jvm) {
        HeapOccupancyAggregation aggregation = jvm.getAggregation(HeapOccupancyAggregation.class).get();
        XYSeriesCollection dataset = aggregation.getHeapOccupancyAfterCollection();
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Heap Occupancy After Collection", "Time (Seconds)", "Occupancy (" + aggregation.getScale() + ")", dataset);
        pane.addTab("Heap", new ChartPanel(chart));
    }

    private void buildPauseTime(JavaVirtualMachine jvm) {
        PauseTimeAggregation aggregation = jvm.getAggregation(PauseTimeAggregation.class).get();
        XYSeriesCollection dataset = aggregation.getPauseTimes();
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Pause Times", "Time (Seconds)", "Pause Time (s)", dataset);
        pane.addTab("Pause Times", new ChartPanel(chart));
    }

    private void buildAllocationRate(JavaVirtualMachine jvm) {
        AllocationRateAggregation aggregation = jvm.getAggregation(AllocationRateAggregation.class).get();
        XYSeriesCollection dataset = aggregation.getAllocationRates();
        Units units = aggregation.getUnits();
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Allocation Rates", "Time (Seconds)", "Allocation Rates " + units.toString() + "/s", dataset);
        pane.addTab("Allocation Rates", new ChartPanel(chart));
    }
}
