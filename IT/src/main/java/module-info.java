// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/*
 Module for the purposes of housing tests that need to be in a module in order to run.
 */
open module com.kodewerk.gcsee.integration {

    requires com.kodewerk.gcsee.api;
    requires java.logging;

    exports com.kodewerk.gcsee.integration.aggregation to
            com.kodewerk.gcsee.api;

    provides com.kodewerk.gcsee.aggregator.Aggregation with
            com.kodewerk.gcsee.integration.aggregation.HeapOccupancyAfterCollectionSummary,
            com.kodewerk.gcsee.integration.aggregation.PauseTimeSummary,
            com.kodewerk.gcsee.integration.aggregation.CollectionCycleCountsSummary,
            com.kodewerk.gcsee.integration.shared.OneRuntimeReport,
            com.kodewerk.gcsee.integration.shared.TwoRuntimeReport,
            com.kodewerk.gcsee.integration.aggregation.CMSCycleAggregation;
}