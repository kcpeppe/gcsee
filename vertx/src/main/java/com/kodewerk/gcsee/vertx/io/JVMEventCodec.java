// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.vertx.io;

import com.kodewerk.gcsee.event.jvm.JVMEvent;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class JVMEventCodec implements MessageCodec<JVMEvent, JVMEvent> {

    public static String NAME = "JVMEvent";

    @Override
    public void encodeToWire(Buffer buffer, JVMEvent jvmEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JVMEvent decodeFromWire(int i, Buffer buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JVMEvent transform(JVMEvent jvmEvent) {
        return jvmEvent;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
