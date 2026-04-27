package com.kodewerk.gcsee.integration;

import com.kodewerk.gcsee.GCSee;
import com.kodewerk.gcsee.integration.io.TestLogFile;
import com.kodewerk.gcsee.integration.shared.OneRuntimeReport;
import com.kodewerk.gcsee.integration.shared.TwoRuntimeReport;
import com.kodewerk.gcsee.io.GCLogFile;
import com.kodewerk.gcsee.io.SingleGCLogFile;
import com.kodewerk.gcsee.jvm.JavaVirtualMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

@Tag("modulePath")
public class TestSharedAggregators {

    private String testLog = "unified/cms/gc.log";

    @Test
    public void compareRuntimeDurations() {
        TestLogFile logFile = new TestLogFile(testLog);
        Path gcLogFile = logFile.getFile().toPath();
        GCLogFile log = new SingleGCLogFile(gcLogFile);
        GCSee toolKit = new GCSee();
        toolKit.loadAggregationsFromServiceLoader();
        JavaVirtualMachine jvm = null;
        try {
            jvm = toolKit.analyze(log);
        } catch (IOException e) {
            Assertions.fail(e);
        }

        jvm.getAggregation(OneRuntimeReport.class).ifPresentOrElse(
                oneRuntimeReport -> Assertions.assertEquals(8.782d, oneRuntimeReport.getRuntimeDuration()),
                () -> Assertions.fail("1 report missing"));

        jvm.getAggregation(TwoRuntimeReport.class).ifPresentOrElse(
                twoRuntimeReport -> Assertions.assertEquals(8.782d, twoRuntimeReport.getRuntimeDuration()),
                () -> Assertions.fail("2 report missing"));
    }
}