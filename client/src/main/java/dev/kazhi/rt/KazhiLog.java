package dev.kazhi.rt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;

/**
 * Writes diagnostics to &lt;gameDir&gt;/kazhi-debug.log so they are visible
 * regardless of how the host routes System.out.
 */
public final class KazhiLog {
    private static Path file;

    private KazhiLog() {}

    public static synchronized void log(String msg) {
        write("INFO  " + msg);
    }

    public static synchronized void error(String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        if (t != null) {
            t.printStackTrace(new PrintWriter(sw));
        }
        write("ERROR " + msg + (t != null ? System.lineSeparator() + sw : ""));
    }

    private static void write(String line) {
        String full = "[" + LocalTime.now() + "] " + line + System.lineSeparator();
        System.out.println("[Kazhi] " + line);
        try {
            Path path = path();
            if (path != null) {
                Files.writeString(path, full, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Path path() {
        if (file == null) {
            try {
                // Fixed, absolute, predictable location for diagnostics.
                file = Path.of(System.getProperty("user.home"), "kazhi-debug.log");
            } catch (Throwable t) {
                file = Path.of("kazhi-debug.log");
            }
        }
        return file;
    }
}
