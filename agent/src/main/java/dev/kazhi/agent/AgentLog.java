package dev.kazhi.agent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;

/** Writes agent-side diagnostics to a fixed absolute path. */
final class AgentLog {
    private static final Path FILE = Path.of(System.getProperty("user.home"), "kazhi-debug.log");

    private AgentLog() {}

    static synchronized void log(String msg) {
        write("AGENT " + msg, null);
    }

    static synchronized void error(String msg, Throwable t) {
        write("AGENT ERROR " + msg, t);
    }

    private static void write(String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        if (t != null) {
            t.printStackTrace(new PrintWriter(sw));
        }
        String line = "[" + LocalTime.now() + "] " + msg
                + (t != null ? System.lineSeparator() + sw : "") + System.lineSeparator();
        System.out.println("[Kazhi] " + msg);
        try {
            Files.writeString(FILE, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {
        }
    }
}
