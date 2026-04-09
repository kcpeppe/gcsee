// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.jvm;

import com.kodewerk.gcsee.event.MemoryPoolSummary;

public class PermGenSummary extends MemoryPoolSummary {

    public PermGenSummary(long before, long after, long size) {
        super(before, after, size);
    }
}
