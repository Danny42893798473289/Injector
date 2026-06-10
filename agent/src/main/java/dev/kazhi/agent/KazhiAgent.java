package dev.kazhi.agent;

import java.lang.instrument.Instrumentation;

public final class KazhiAgent {
    private KazhiAgent() {}

    public static void agentmain(String args, Instrumentation instrumentation) {
        RuntimeInjector.inject(instrumentation, args);
    }

    public static void premain(String args, Instrumentation instrumentation) {
        agentmain(args, instrumentation);
    }
}
