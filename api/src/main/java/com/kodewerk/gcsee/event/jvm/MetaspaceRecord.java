// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.jvm;

import com.kodewerk.gcsee.event.MemoryPoolSummary;

public class MetaspaceRecord extends MemoryPoolSummary {

    public MetaspaceRecord(long before, long after, long size) {
        super(before, after, size);
    }

    public MetaspaceRecord(long occupancyBefore, long configuredSizeBefore, long occupancyAfter, long configuredSizeAfter) {
        super(occupancyBefore,configuredSizeBefore,occupancyAfter,configuredSizeAfter);
    }
}
