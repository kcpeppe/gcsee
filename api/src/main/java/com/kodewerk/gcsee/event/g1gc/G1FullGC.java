// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.g1gc;

import com.kodewerk.gcsee.event.GCCause;
import com.kodewerk.gcsee.event.GarbageCollectionTypes;
import com.kodewerk.gcsee.time.DateTimeStamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class G1FullGC extends G1RealPause {

    private final Map<String, Double> internalPhaseTimes = new ConcurrentHashMap<>();

    public G1FullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
        this(timeStamp, GarbageCollectionTypes.Full, cause, pauseTime);
    }

    public G1FullGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double pauseTime) {
        super(timeStamp, type, cause, pauseTime);
    }

    public void addInternalPhase(String internalPhase, Double duration) {
        internalPhaseTimes.put(internalPhase, duration);
    }

    public Stream<String> internalPhases() {
        return internalPhaseTimes.keySet().stream();
    }

    public double internalPhaseDuration(String phaseName) {
        return internalPhaseTimes.get(phaseName);
    }
}
