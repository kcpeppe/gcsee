package com.kodewerk.gcsee.integration;

import com.kodewerk.gcsee.GCSee;
import com.kodewerk.gcsee.integration.aggregation.CMSCycleAggregation;
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
public class CMSEventsTest {
    @Test
    public void testMain() {
        Path path = new TestLogFile("preunified/cms/parnew/details/scavangeBeforeRemarkWithReference.log").getFile().toPath();
        analyze(path.toString());
    }

    public void analyze(String gcLogFile) {
        /**
         * GC log files can come in  one of two types: single or series of rolling logs.
         * In this sample, we load a single log file.
         * The log files can be either in text, zip, or gzip format.
         */
        GCLogFile logFile = new SingleGCLogFile(Path.of(gcLogFile));
        GCSee gcSee = new GCSee();

        /**
         * This call will load all implementations of Aggregator that have been declared in module-info.java.
         * This mechanism makes use of Module SPI.
         */
        gcSee.loadAggregationsFromServiceLoader();

        /**
         * The JavaVirtualMachine contains the aggregations as filled out by the Aggregators.
         * It also contains configuration information about how the JVM was configured for the runtime.
         */
        JavaVirtualMachine machine = null;
        try {
            machine = gcSee.analyze(logFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        machine.getAggregation(CMSCycleAggregation.class).ifPresent(cmsCycleCounts -> {
            Assertions.assertEquals( 1, cmsCycleCounts.getInitialMark(), "Initial Mark events count");
            Assertions.assertEquals( 1, cmsCycleCounts.getRemark(), "Remark events count");
            Assertions.assertEquals( 5, cmsCycleCounts.getConcurrentEvent(), "concurrent phase events count");
        });

    }
}
