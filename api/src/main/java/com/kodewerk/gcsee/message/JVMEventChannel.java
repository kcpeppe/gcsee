// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.message;

import com.kodewerk.gcsee.event.jvm.JVMEvent;

public interface JVMEventChannel extends Channel<JVMEvent,JVMEventChannelListener> {
}
