package com.kodewerk.gcsee.integration.core;

import com.kodewerk.gcsee.GCSee;
import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregation;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.Collates;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.integration.io.TestLogFile;
import com.kodewerk.gcsee.io.GCLogFile;
import com.kodewerk.gcsee.io.SingleGCLogFile;
import com.kodewerk.gcsee.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class PreunifiedJavaVirtualMachineConfigurationTest {

    private String logFile = "preunified/g1gc/details/tenuring/180/g1gc.log";
    // Indexes: [0] machine.getEstimatedJVMStartTime, [1] machine.getTimeOfFirstEvent,
    // [2] aggregation.timeOfTerminationEvent, [3] aggregation.estimatedRuntime — all in ms.
    // [3] is termination − 0 (log first uptime 1.028s ≤ 120s ε ⇒ start treated as 0).
    private int[] times = { 0, 1028, 945481, 945481};

    @Tag("modulePath")
    @Test
    public void testSingle() {
        TestLogFile log = new TestLogFile(logFile);
        smokeTest(new SingleGCLogFile(log.getFile().toPath()), times);
    }

    private void smokeTest(GCLogFile log, int[] endStartTimes ) {
        GCSee gcSee = new GCSee();
        gcSee.loadAggregationsFromServiceLoader();
        TestTimeAggregation aggregation = new TestTimeAggregation();
        gcSee.loadAggregation(aggregation);
        JavaVirtualMachine machine = null;
        try {
            machine = gcSee.analyze(log);
            aggregation = machine.getAggregation(PreunifiedJavaVirtualMachineConfigurationTest.TestTimeAggregation.class).get();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            machine.getEstimatedJVMStartTime();
            machine.getTimeOfFirstEvent().getTimeStamp();
            aggregation.timeOfTerminationEvent().getTimeStamp();
            aggregation.estimatedRuntime();
        } catch(Throwable t) {
            fail("Failed to extract runtime timing information",t);
        }

        Assertions.assertEquals( endStartTimes[0], (int)(machine.getEstimatedJVMStartTime().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[1], (int)(machine.getTimeOfFirstEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[2], (int)(aggregation.timeOfTerminationEvent().getTimeStamp() * 1000.0d));
        Assertions.assertEquals( endStartTimes[3], (int)(aggregation.estimatedRuntime() * 1000.0d));
    }

    @Aggregates({EventSource.G1GC,EventSource.GENERATIONAL,EventSource.ZGC,EventSource.SHENANDOAH})
    public static class TestTimeAggregator extends Aggregator<TestTimeAggregation> {

        /**
         * Subclass only.
         *
         * @param aggregation The Aggregation that {@literal @}Collates this Aggregator
         * @see Collates
         * @see Aggregation
         */
        public TestTimeAggregator(TestTimeAggregation aggregation) {
            super(aggregation);
        }
    }

    @Collates(TestTimeAggregator.class)
    public static class TestTimeAggregation extends Aggregation {

        public TestTimeAggregation() {}

        @Override
        public boolean hasWarning() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }
}
