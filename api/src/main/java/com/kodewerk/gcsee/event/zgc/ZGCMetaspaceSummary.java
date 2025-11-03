// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.zgc;

public class ZGCMetaspaceSummary {
    private final long used;
    private final long committed;
    private final long reserved;

    public ZGCMetaspaceSummary(long used, long committed, long reserved) {
        this.used = used;
        this.committed = committed;
        this.reserved = reserved;
    }

    public long getUsed() {
        return used;
    }

    public long getCommitted() {
        return committed;
    }

    public long getReserved() {
        return reserved;
    }
}
