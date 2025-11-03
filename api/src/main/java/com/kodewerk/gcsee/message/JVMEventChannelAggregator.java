// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.message;

import com.kodewerk.gcsee.aggregator.Aggregator;
import com.kodewerk.gcsee.event.jvm.JVMEvent;

public class JVMEventChannelAggregator implements JVMEventChannelListener {

    private ChannelName channel;
    private Aggregator aggregator;

    public JVMEventChannelAggregator(ChannelName channel, Aggregator aggregator) {
        this.channel = channel;
        this.aggregator = aggregator;
    }

    @Override
    public ChannelName channel() {
        return channel;
    }

    @Override
    public void receive(JVMEvent payload) {
        aggregator.receive(payload);
    }
}
