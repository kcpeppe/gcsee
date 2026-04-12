// Copyright (c) Microsoft Corporation.
// Copyright (c) Kirk Pepperdine
// Licensed under the MIT License.
package com.kodewerk.gcsee.io;

import com.kodewerk.gcsee.jvm.Diarizer;
import com.kodewerk.gcsee.jvm.Diary;
import com.kodewerk.gcsee.jvm.JavaVirtualMachine;
import com.kodewerk.gcsee.jvm.PreUnifiedJavaVirtualMachine;
import com.kodewerk.gcsee.jvm.UnifiedJavaVirtualMachine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static java.util.ServiceLoader.Provider;
import static java.util.ServiceLoader.load;


/**
 * Represents a GC log file, which may be a {@link SingleGCLogFile} or a {@link RotatingGCLogFile}.
 * <p>
 * Metadata about the log file — including whether it uses unified logging format — is derived
 * from the {@link Diary} produced by running a {@link Diarizer} over the log contents.
 * Callers interact with {@code GCLogFile} directly; the {@code Diary} answers under the hood.
 */
public abstract class GCLogFile extends FileDataSource<String> {

    private static final Logger LOGGER = Logger.getLogger(FileDataSource.class.getName());

    /**
     * The value used for the implementation of {@link #endOfData()}.
     */
    public static final String END_OF_DATA_SENTINEL = "END_OF_DATA_SENTINEL";

    private Diary diary;
    private JavaVirtualMachine jvm = null;

    /**
     * Subclass only.
     * @param path The path to the GCLogFile or, in the case of rotating log files, the parent directory.
     */
    protected GCLogFile(Path path) {
        super(path);
    }

    /**
     * Return the relevant JavaVirtualMachine implementation.
     * The choice of implementation is driven by the Diary.
     */
    public JavaVirtualMachine getJavaVirtualMachine() throws IOException {
        if (jvm == null) {
            jvm = isUnified() ? new UnifiedJavaVirtualMachine() : new PreUnifiedJavaVirtualMachine();
            jvm.accepts(this);
        }
        return jvm;
    }

    /**
     * Returns {@code true} if this GCLogFile is written in unified logging (JEP 158) format.
     * <p>
     * The answer is derived from the {@link Diary} — format discovery is performed by the
     * {@link Diarizer} rather than by this class directly.
     *
     * @return {@code true} if the log file is in unified logging format.
     */
    public boolean isUnified() throws IOException {
        return diary().isUnifiedLogging();
    }

    /**
     * Lazily computes and caches the {@link Diary} for this log file.
     * <p>
     * On first call, loads all available {@link Diarizer} implementations via the
     * {@link ServiceLoader} and runs the stream through them. The {@link Diarizer}
     * self-selects based on what it sees in the log — no prior knowledge of the
     * log format is required.
     *
     * @return the computed diary
     */
    public Diary diary() throws IOException {
        if (diary == null) {
            Diarizer diarizer = diarizer();
            stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> s.length() > 0)
                    .map(diarizer::diarize)
                    .filter(completed -> completed)
                    .findFirst();
            this.diary = diarizer.getDiary();
        }
        return diary;
    }

    /**
     * Selects a {@link Diarizer} without requiring prior knowledge of the log format.
     * <p>
     * Rather than pre-determining the format and then selecting the Diarizer, we let
     * the Diarizer discover the format itself — inverting the control compared to the
     * previous approach.
     */
    private Diarizer diarizer() throws IOException {
        ServiceLoader<Diarizer> serviceLoader = load(Diarizer.class);
        if (serviceLoader.findFirst().isPresent()) {
            return serviceLoader
                    .stream()
                    .map(Provider::get)
                    .findFirst()
                    .orElseThrow(() -> new ServiceConfigurationError("Unable to find a suitable Diarizer"));
        } else {
            // Fallback: classpath mode — load via classloader without module system
            try {
                String clazzName = "com.kodewerk.gcsee.parser.jvm.UnifiedDiarizer";
                Class<?> clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
                return (Diarizer) clazz.getConstructors()[0].newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ServiceConfigurationError("Unable to find a suitable Diarizer", e);
            }
        }
    }

    @Override
    public final String endOfData() {
        return END_OF_DATA_SENTINEL;
    }
}
