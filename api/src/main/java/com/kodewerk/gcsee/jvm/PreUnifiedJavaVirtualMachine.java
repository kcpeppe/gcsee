// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.jvm;

import com.kodewerk.gcsee.io.DataSource;
import com.kodewerk.gcsee.io.GCLogFile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of JavaVirtualMachine that uses io.vertx verticles to feed
 * lines to the parser(s) and post events to the aggregators. This implementation
 * is here in the vertx module so that the api and parser modules can exist without
 * having to import io.vertx. In the api module, the class GCSee uses the classloader
 * to load required JavaVirtualMachine.
 */
public class PreUnifiedJavaVirtualMachine extends AbstractJavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedJavaVirtualMachine.class.getName());

    // Safepoint details are in a different log and would be loaded separately pre-unified.
    @Override
    public boolean accepts(DataSource logFile) {
        try {
            if (logFile instanceof GCLogFile) {
                if (!((GCLogFile) logFile).isUnified()) {
                    super.setDataSource(logFile);
                    return true;
                }
            }
        } catch(IOException ioe) {
            LOGGER.log(Level.WARNING, ioe.getMessage());
        }

        return false;
    }

    @Override
    public boolean isUnifiedLogging() {
        return false;
    }

    @Override
    public boolean isZGC() {
        return false;
    }

    @Override
    public boolean isShenandoah() {
        return false;
    }

}
