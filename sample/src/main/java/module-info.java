// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains an Aggregator and an Aggregation
 */
module com.kodewerk.gcsee.sample {
    requires com.kodewerk.gcsee.api;
    requires java.logging;

    exports com.kodewerk.gcsee.sample;

    exports com.kodewerk.gcsee.sample.aggregation to
            com.kodewerk.gcsee.api;

    provides com.kodewerk.gcsee.aggregator.Aggregation with
             com.kodewerk.gcsee.sample.aggregation.HeapOccupancyAfterCollectionSummary,
             com.kodewerk.gcsee.sample.aggregation.PauseTimeSummary,
             com.kodewerk.gcsee.sample.aggregation.CollectionCycleCountsSummary;
}
