// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.message;

import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.jvm.Diary;

import java.util.Set;

public interface DataSourceParser extends DataSourceChannelListener {
    void publishTo(JVMEventChannel channel);
    void diary(Diary diary);
    boolean accepts(Diary diary);
    Set<EventSource> eventsProduced();
}
