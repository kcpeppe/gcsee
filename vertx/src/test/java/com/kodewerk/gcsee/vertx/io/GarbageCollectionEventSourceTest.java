// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.vertx.io;

import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GCEvent;
import com.kodewerk.gcsee.event.generational.DefNew;
import com.kodewerk.gcsee.io.GCLogFile;
import com.kodewerk.gcsee.io.RotatingGCLogFile;
import com.kodewerk.gcsee.io.SingleGCLogFile;
import com.kodewerk.gcsee.jvm.Diary;
import com.kodewerk.gcsee.message.ChannelName;
import com.kodewerk.gcsee.message.DataSourceParser;
import com.kodewerk.gcsee.message.JVMEventChannel;
import com.kodewerk.gcsee.time.DateTimeStamp;
import com.kodewerk.gcsee.vertx.VertxDataSourceChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GarbageCollectionEventSourceTest {

    private static final String END_OF_DATA_SENTINEL = GCLogFile.END_OF_DATA_SENTINEL;

    private GCLogFile loadLogFile(Path path, boolean rotating) {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }
    
    @Test
    public void testRotatingLogDirectory() {
        Path path = new TestLogFile("rotating_directory").getFile().toPath();
        assertExpectedLineCountInLog(72210, loadLogFile(path, true));

    }

    @Test
    public void testPlainTextFileLineCount() {
        Path path = new TestLogFile("streaming/gc.log").getFile().toPath();
        assertExpectedLineCountInLog(431604, new SingleGCLogFile(path));
    }

    @Test
    public void testGZipTarFileLineCount() {
        Path path = new TestLogFile("streaming/gc.log.tar.gz").getFile().toPath();
        assertExpectedLineCountInLog(410055, loadLogFile(path, false));
    }

    @Test
    public void testSingleLogInZipLineCount() {
        Path path = new TestLogFile("streaming/gc.log.zip").getFile().toPath();
        assertExpectedLineCountInLog(431604, loadLogFile(path, false));
    }

    @Test
    public void testRotatingLogsLineCount() {
        Path path = new TestLogFile("rotating.zip").getFile().toPath();
        assertExpectedLineCountInLog(72210, loadLogFile(path, true));
    }

    @Test
    public void testRotatingLogsRotatingLineCount() {
        Path path = new TestLogFile("rotating.zip").getFile().toPath();
        assertExpectedLineCountInLog(72210, loadLogFile(path, true));
    }

    /*
    72209 lines + EOF sentinal.
     */
    @Test
    public void testZippedDirectoryWithRotatingLogRotatingLineCount() {
        Path path = new TestLogFile("streaming/rotating_directory.zip").getFile().toPath();
        assertExpectedLineCountInLog(72209 + 1, loadLogFile(path, true));
    }

    private static void disableCaching() {
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
    }

    private void assertExpectedLineCountInLog(int expectedNumberOfLines, GCLogFile logFile) {
        disableCaching();
        GCLogConsumer consumer = new GCLogConsumer();
        VertxDataSourceChannel channel = new VertxDataSourceChannel();
        channel.registerListener(consumer);
        long[] observedNumberOfLines = {0L};
        try {
            logFile.stream().forEach(message -> {
                observedNumberOfLines[0]++;
                channel.publish(ChannelName.DATA_SOURCE, message);
            });
        } catch (IOException e) {
            fail(e.getMessage());
        }
        consumer.awaitEOF();
        assertEquals(expectedNumberOfLines, observedNumberOfLines[0]);
        assertEquals(expectedNumberOfLines, consumer.getEventCount());
    }

    private class GCLogConsumer implements DataSourceParser {

        private final CountDownLatch eof = new CountDownLatch(1);
        private volatile int eventCount = 0;

        @Override
        public ChannelName channel() {
            return ChannelName.DATA_SOURCE;
        }

        @Override
        public void receive(String payload) {
            eventCount++;
            if ( END_OF_DATA_SENTINEL.equals(payload)) {
                    eof.countDown();
            }
        }

        public void awaitEOF() {
            try {
                eof.await();
            } catch (InterruptedException e) {
                Thread.interrupted();
                fail(e);
            }
        }

        GCLogConsumer() {
        }

        int getEventCount() {
            return eventCount;
        }

        @Override
        public void publishTo(JVMEventChannel channel) {
            throw new IllegalStateException();
        }

        @Override
        public void diary(Diary diary) {
            throw new IllegalStateException();
        }

        @Override
        public boolean accepts(Diary diary) {
            return false;
        }

        @Override
        public Set<EventSource> eventsProduced() {
            return Set.of();
        }
    }

    @Test
    public void testEqualsForDifferentObject() {
        GCEvent gcEvent = new DefNew(new DateTimeStamp("2018-04-04T09:10:00.586-0100"), GCCause.WARMUP,102);
        assertNotEquals(gcEvent, new ArrayList<>());
    }
}
