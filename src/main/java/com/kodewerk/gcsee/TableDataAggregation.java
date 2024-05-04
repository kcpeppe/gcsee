package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregation;

public class TableDataAggregation extends Aggregation {

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
