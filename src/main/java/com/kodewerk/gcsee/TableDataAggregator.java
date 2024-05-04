package com.kodewerk.gcsee;

import com.microsoft.gctoolkit.aggregator.Aggregator;

public class TableDataAggregator extends Aggregator<TableDataAggregation> {
    protected TableDataAggregator(TableDataAggregation aggregation) {
        super(aggregation);
    }
}
