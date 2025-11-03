// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.

import com.kodewerk.gcsee.jvm.PreUnifiedJavaVirtualMachine;
import com.kodewerk.gcsee.jvm.UnifiedJavaVirtualMachine;

/*
 * Contains the core API for the Microsoft, Java Garbage Collection Toolkit.
 * The toolkit is a GC log parser and a framework for consuming and extracting data from
 * GC log events.
 * <p>
 * The main entry points are:
 * <dl>
 * <dt>{@link com.kodewerk.gcsee.GCSee}</dt>
 * <dd>This is the main API that an application will use.</dd>
 * <dt>{@link com.kodewerk.gcsee.io.GCLogFile}</dt>
 * <dd>A GCLogFile is passed to GCSee for analysis.</dd>
 * <dt>{@link com.kodewerk.gcsee.jvm.JavaVirtualMachine}</dt>
 * <dd>This contains the results from running an analysis on a GC log.</dd>
 * <dt>{@link com.kodewerk.gcsee.event.jvm.JVMEvent}</dt>
 * <dd>The parser generates JVMEvents.</dd>
 * <dt>{@link com.kodewerk.gcsee.aggregator.Aggregator}</dt>
 * <dd>An Aggregator captures JVMEvents for analysis.</dd>
 * <dt>{@link com.kodewerk.gcsee.aggregator.Aggregation}</dt>
 * <dd>An Aggregation works with an Aggregator to collect and analyze data from JVMEvents.</dd>
 * </dl>
 */
 /**
 * @uses com.kodewerk.gcsee.jvm.JavaVirtualMachine
 * @uses com.kodewerk.gcsee.aggregator.Aggregator
 */
module com.kodewerk.gcsee.api {
    requires java.logging;

    exports com.kodewerk.gcsee;
    exports com.kodewerk.gcsee.aggregator;
    exports com.kodewerk.gcsee.event;
    exports com.kodewerk.gcsee.event.g1gc;
    exports com.kodewerk.gcsee.event.generational;
    exports com.kodewerk.gcsee.event.jvm;
    exports com.kodewerk.gcsee.event.shenandoah;
    exports com.kodewerk.gcsee.event.zgc;
    exports com.kodewerk.gcsee.io;
    exports com.kodewerk.gcsee.jvm;
    exports com.kodewerk.gcsee.time;
    exports com.kodewerk.gcsee.message;

    uses com.kodewerk.gcsee.aggregator.Aggregation;
    uses com.kodewerk.gcsee.jvm.JavaVirtualMachine;
    uses com.kodewerk.gcsee.jvm.Diarizer;
    uses com.kodewerk.gcsee.message.DataSourceParser;
    uses com.kodewerk.gcsee.message.DataSourceChannel;
    uses com.kodewerk.gcsee.message.DataSourceChannelListener;
    uses com.kodewerk.gcsee.message.JVMEventChannel;
    uses com.kodewerk.gcsee.message.JVMEventChannelListener;

    // todo: no need to load with SPI
    provides com.kodewerk.gcsee.jvm.JavaVirtualMachine with
            PreUnifiedJavaVirtualMachine,
            UnifiedJavaVirtualMachine;
}
