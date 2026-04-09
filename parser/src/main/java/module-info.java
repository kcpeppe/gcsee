// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the GCSee GC log parser. The parser is an internal module.
 */
module com.kodewerk.gcsee.parser {
    requires com.kodewerk.gcsee.api;
    requires java.logging;

    exports com.kodewerk.gcsee.parser to
            com.kodewerk.gcsee.api;

    exports com.kodewerk.gcsee.parser.io to
            com.kodewerk.gcsee.api;

    exports com.kodewerk.gcsee.parser.jvm to
            com.kodewerk.gcsee.api;

    exports com.kodewerk.gcsee.parser.unified to
            com.kodewerk.gcsee.api;

    exports com.kodewerk.gcsee.parser.vmops to
            com.kodewerk.gcsee.api;

    provides com.kodewerk.gcsee.jvm.Diarizer with
            com.kodewerk.gcsee.parser.jvm.PreUnifiedDiarizer,
            com.kodewerk.gcsee.parser.jvm.UnifiedDiarizer;

    provides com.kodewerk.gcsee.message.DataSourceParser with
            com.kodewerk.gcsee.parser.JVMEventParser,
            com.kodewerk.gcsee.parser.UnifiedJVMEventParser,
            com.kodewerk.gcsee.parser.vmops.SafepointParser,
            com.kodewerk.gcsee.parser.SurvivorMemoryPoolParser,
            com.kodewerk.gcsee.parser.UnifiedSurvivorMemoryPoolParser,
            com.kodewerk.gcsee.parser.CMSTenuredPoolParser,
            com.kodewerk.gcsee.parser.GenerationalHeapParser,
            com.kodewerk.gcsee.parser.UnifiedGenerationalParser,
            com.kodewerk.gcsee.parser.PreUnifiedG1GCParser,
            com.kodewerk.gcsee.parser.UnifiedG1GCParser,
            com.kodewerk.gcsee.parser.ShenandoahParser,
            com.kodewerk.gcsee.parser.ZGCParser;
}