package com.kodewerk.gcsee.integration;

import com.kodewerk.gcsee.GCSee;
import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregation;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.Collates;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.integration.aggregation.CollectionCycleCountsSummary;
import com.kodewerk.gcsee.integration.aggregation.HeapOccupancyAfterCollectionSummary;
import com.kodewerk.gcsee.integration.io.TestLogFile;
import com.kodewerk.gcsee.io.GCLogFile;
import com.kodewerk.gcsee.io.SingleGCLogFile;
import com.kodewerk.gcsee.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("modulePath")
public class ZeroAggregationTest {

    @Test
    public void testNoAggregationRegistered() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        /*
         * GC log files can come in  one of two types: single or series of rolling logs.
         * In this sample, we load a single log file.
         * The log files can be either in text, zip, or gzip format.
         */
        GCLogFile logFile = new SingleGCLogFile(path);
        GCSee gcSee = new GCSee();
        // Do not call GCSee::loadAggregationsFromServiceLoader
        JavaVirtualMachine machine = null;
        try {
            machine = gcSee.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        Assertions.assertTrue(machine.getAggregation(HeapOccupancyAfterCollectionSummary.class).isEmpty());
        Assertions.assertTrue(machine.getAggregation(CollectionCycleCountsSummary.class).isEmpty());
    }

    @Tag("modulePath")
    @Test
    public void testSuppliedAggregation() {
        Path path = new TestLogFile("cms/defnew/details/defnew.log").getFile().toPath();
        GCLogFile logFile = new SingleGCLogFile(path);
        GCSee gcSee = new GCSee();
        // Load our local Aggregation that will not be registered for the given log file
        gcSee.loadAggregation(new ZeroAggregationTest.TestAggregation());
        JavaVirtualMachine machine = null;
        try {
            machine = gcSee.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // Retrieves the Aggregation for HeapOccupancyAfterCollectionSummary. This is a time-series aggregation.
        Assertions.assertTrue(machine.getAggregation(ZeroAggregationTest.TestAggregation.class).isEmpty());

    }

    @Collates(ZeroAggregationTest.TestAggregator.class)
    public static class TestAggregation extends Aggregation {

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    @Aggregates(EventSource.G1GC)
    public static class TestAggregator extends Aggregator<TestAggregation> {

        /**
         * Subclass only.
         *
         * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
         * @see Collates
         * @see Aggregation
         */
        protected TestAggregator(TestAggregation aggregation) {
            super(aggregation);
        }
    }
}
