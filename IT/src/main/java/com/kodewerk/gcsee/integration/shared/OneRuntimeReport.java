package com.kodewerk.gcsee.integration.shared;

import com.kodewerk.gcsee.aggregator.Collates;

@Collates(OneRuntimeAggregator.class)
public class OneRuntimeReport extends SharedAggregation {


    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
