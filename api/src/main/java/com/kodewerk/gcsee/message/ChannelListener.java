package com.kodewerk.gcsee.message;

public interface ChannelListener<M> {
    ChannelName channel();
    void receive(M payload);
}
