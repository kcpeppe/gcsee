package com.kodewerk.gcsee.integration.aggregation;

import com.kodewerk.gcsee.aggregator.Aggregates;
import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.generational.CMSConcurrentEvent;
import com.kodewerk.gcsee.event.generational.CMSRemark;
import com.kodewerk.gcsee.event.generational.InitialMark;


@Aggregates({EventSource.GENERATIONAL})
public class CMSCycleAggregator extends Aggregator<CMSCycleAggregation> {

    private InitialMark lastInitialMark = null;
    private CMSRemark lastRemark = null;
    public CMSCycleAggregator(CMSCycleAggregation results) {
        super(results);
        register(InitialMark.class, this::count);
        register(CMSRemark.class, this::count);
        register(CMSConcurrentEvent.class, this::count);
    }

    public void count(InitialMark event) {
        if ( event.equals(lastInitialMark)) return;
        lastInitialMark = event;
        aggregation().initialMark();
    }

    public void count(CMSRemark event) {
        if ( event.equals(lastRemark)) return;
        lastRemark = event;
        aggregation().remark();
    }

    public void count(CMSConcurrentEvent event) {
        aggregation().concurrentEvent();
    }
}
