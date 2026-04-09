package com.kodewerk.gcsee.integration.shared;

import com.kodewerk.gcsee.aggregator.Aggregation;

public abstract class SharedAggregation extends Aggregation {

    public double getRuntimeDuration() { return super.estimatedRuntime(); }

}
