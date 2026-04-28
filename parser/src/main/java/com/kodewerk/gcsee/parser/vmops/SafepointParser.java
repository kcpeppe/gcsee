// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.parser.vmops;

import com.kodewerk.gcsee.aggregator.EventSource;
import com.kodewerk.gcsee.event.jvm.JVMTermination;
import com.kodewerk.gcsee.event.jvm.Safepoint;
import com.kodewerk.gcsee.jvm.Diary;
import com.kodewerk.gcsee.message.ChannelName;
import com.kodewerk.gcsee.message.JVMEventChannel;
import com.kodewerk.gcsee.parser.PreUnifiedGCLogParser;

import java.util.Set;

public class SafepointParser extends PreUnifiedGCLogParser implements SafepointPatterns {

    public SafepointParser() {}

    @Override
    public Set<EventSource> eventsProduced() {
        return Set.of(EventSource.SAFEPOINT);
    }

    public String getName() {
        return "SafepointParser";
    }

    protected void process(String line) {
        SafepointTrace trace;
        if ((trace = TRACE.parse(line)) != null) {
            Safepoint safepoint = trace.toSafepoint();
            super.publish(ChannelName.JVM_EVENT_PARSER_OUTBOX, safepoint);
        } else if (line.equals(END_OF_DATA_SENTINEL))
            super.publish( ChannelName.JVM_EVENT_PARSER_OUTBOX, new JVMTermination(getClock(),diary.getEstimatedStartTime()));
    }

    @Override
    public boolean accepts(Diary diary) {
        return (diary.isTLABData() || diary.isApplicationStoppedTime() || diary.isApplicationRunningTime()) && ! diary.isUnifiedLogging();
    }

    @Override
    public void publishTo(JVMEventChannel bus) {
        super.publishTo(bus);
    }
}

