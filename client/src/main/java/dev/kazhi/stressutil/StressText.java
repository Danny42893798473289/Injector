package dev.kazhi.stressutil;

public final class StressText {
    private StressText() {}

    public static String line(int length) {
        return "A".repeat(Math.max(1, length));
    }

    public static String[] signLines(int lineLength) {
        String line = line(lineLength);
        return new String[]{line, line, line, line};
    }
}
