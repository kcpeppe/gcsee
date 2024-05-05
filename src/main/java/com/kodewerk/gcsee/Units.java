package com.kodewerk.gcsee;

public enum Units {
    KB("KB"),
    MB("MB"),
    GB("GB");

    public static Units scaleFromKB(double value) {
        Units currentScale = KB;
        double scaledValue = value;
        while ( scaledValue > 1024.0 && currentScale != GB) {
            scaledValue = scaledValue / 1024.0d;
             currentScale = switch (currentScale) {
                 case KB -> MB;
                 case MB -> GB;
                 default -> throw new IllegalStateException("Unexpected value: " + currentScale);
             };
        }
        return currentScale;
    }

    private final String label;

    Units(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}
