package com.kodewerk.gcsee.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.module.ModuleDescriptor;

@Tag("modulePath")
public class DiagnoseProvidersTest {

    @Test
    public void dump() {
        Module self = getClass().getModule();
        System.out.println("=== self module: " + self.getName()
                + " (named=" + self.isNamed() + ")");
        System.out.println("=== gcsee modules in boot layer ===");
        self.getLayer().modules().stream()
                .filter(m -> m.getName() != null && m.getName().startsWith("com.kodewerk.gcsee"))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .forEach(m -> {
                    ModuleDescriptor d = m.getDescriptor();
                    System.out.println("  " + d.name()
                            + (d.isAutomatic() ? " [automatic]" : "")
                            + " uses=" + d.uses().size()
                            + " provides=" + d.provides().size());
                    d.provides().forEach(p -> {
                        System.out.println("    provides " + p.service());
                        p.providers().forEach(pr -> System.out.println("      with " + pr));
                    });
                    d.uses().forEach(u -> System.out.println("    uses " + u));
                });
    }
}
