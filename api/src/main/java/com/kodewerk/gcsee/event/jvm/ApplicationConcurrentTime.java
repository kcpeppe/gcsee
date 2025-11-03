// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.jvm;

import com.kodewerk.gcsee.time.DateTimeStamp;

/**
 * Event to report on time application is running with the collector
 */
public class ApplicationConcurrentTime extends JVMEvent {

    /**
     * @param timeStamp start of event
     * @param duration duration of the event
     */
    public ApplicationConcurrentTime(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

}
