// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.jvm;

import com.kodewerk.gcsee.time.DateTimeStamp;

public interface Diarizer {

    int MAXIMUM_LINES_TO_EXAMINE = 10_000;

    String getCommandLine();

    DateTimeStamp getTimeOfFirstEvent();

    int getMaxTenuringThreshold();

    boolean isUnified();

    Diary getDiary();

    boolean hasJVMEvents();

    boolean completed();

    boolean diarize(String line);

}
