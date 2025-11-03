// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.message;

public interface ChannelListener<M> {
    ChannelName channel();
    void receive(M payload);
}
