// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event;

public class RegionSummary {

    private final int before;
    private final int after;
    private final int assigned;

    public RegionSummary(int before, int after, int assigned) {
        this.before = before;
        this.after = after;
        this.assigned = assigned;
    }

    public int getBefore() {
        return this.before;
    }

    public int getAfter() {
        return this.after;
    }

    public int getAssigned() {
        return this.assigned;
    }
}
