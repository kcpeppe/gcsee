// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.kodewerk.gcsee.parser.vmops;


import com.kodewerk.gcsee.parser.PreUnifiedTokens;
import com.kodewerk.gcsee.parser.SafepointParseRule;

/**
 * General format is;
 * vmop                    [threads: total initially_running wait_to_block]    [time: spin block sync cleanup vmop] page_trap_count
 */
public interface SafepointPatterns extends PreUnifiedTokens {

    String VMOP = "(Deoptimize|no vm operation|EnableBiasedLocking|GenCollectForAllocation|RevokeBias|BulkRevokeBias|ThreadDump|FindDeadlocks|Exit)";
    String SAFE_POINT_PREFIX = TIMESTAMP + VMOP;
    String THREAD_COUNTS = "\\[\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+\\]";
    String TIMINGS = "\\[\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+" + COUNTER + "\\s+\\]";

    SafepointParseRule TRACE = new SafepointParseRule(SAFE_POINT_PREFIX + "\\s+" + THREAD_COUNTS + "\\s+" + TIMINGS + "\\s+" + COUNTER);
}
