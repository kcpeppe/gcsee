// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.event.zgc;

import com.kodewerk.gcsee.event.LabelledGCEventType;

public enum ZGCConcurrentPhases implements LabelledGCEventType {

    MARK("Concurrent Mark"),
    REFERENCE_PROCESSING( "Reference Processing"),
    RELOCATION_SET_SELECTION( "Relocation Set Selection"),
    RELOCATE( "Relocate");

    private final String label;

    ZGCConcurrentPhases(String label) {
        this.label = label;
    }

    public static ZGCConcurrentPhases fromLabel(String label) {
        return LabelledGCEventType.fromLabel(ZGCConcurrentPhases.class, label);
    }

    public String getLabel() {
        return label;
    }
}