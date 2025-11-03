// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.zgc;

public class ZGCPromotedSummary {
    private final long relocateStart;
    private final long relocateEnd;

    public ZGCPromotedSummary(long relocateStart, long relocateEnd) {

        this.relocateStart = relocateStart;
        this.relocateEnd = relocateEnd;
    }

    public long getRelocateEnd() {
        return relocateEnd;
    }

    public long getRelocateStart() {
        return relocateStart;
    }
}
