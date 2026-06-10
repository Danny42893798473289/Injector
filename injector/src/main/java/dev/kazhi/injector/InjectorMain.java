package dev.kazhi.injector;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class InjectorMain {
    public static final String VERSION = "1.2.0";

    private static final List<String> PROCESS_HINTS = List.of(
            "minecraft", "netease", "neoforge", "java", "modlauncher", "lwjgl"
    );

    private static final List<String> GAME_MATCHERS = List.of(
            "neoforgeclient", "bootstraplauncher", "net.minecraft.client.main.main", "--fml.mcversion"
    );

    public static void main(String[] args) {
        String command = args.length == 0 ? "auto" : args[0].toLowerCase(Locale.ROOT);
        try {
            switch (command) {
                case "auto" -> autoInject(args);
                case "list" -> listProcesses();
                case "inject" -> inject(args);
                case "help", "-h", "--help" -> printUsage();
                default -> {
                    System.err.println("Unknown command: " + command);
                    printUsage();
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            pause();
            System.exit(1);
        }
    }

    private static void autoInject(String[] args) throws Exception {
        System.out.println("Kazhi runtime injector v" + VERSION + " (MC 1.21.8 NeoForge)");
        Path agentJar = JarLocator.findAgentJar();
        Path clientJar = JarLocator.findClientJar();
        System.out.println("Agent:   " + agentJar.getFileName());
        System.out.println("Payload: " + clientJar.getFileName());
        System.out.println();
        System.out.println("Searching for a running Minecraft (NetEase / NeoForge) process...");
        System.out.println("(If the game isn't open yet, start it now and stop at the main menu.)");

        VirtualMachineDescriptor target = waitForMinecraft(90);
        if (target == null) {
            System.err.println("No Minecraft process found. Start the game first, then run this again.");
            pause();
            return;
        }

        System.out.println("Found Minecraft (PID " + target.id() + "). Injecting into the live game...");
        VirtualMachine vm = VirtualMachine.attach(target);
        try {
            vm.loadAgent(agentJar.toAbsolutePath().toString(), "");
        } finally {
            vm.detach();
        }

        System.out.println();
        System.out.println("Injected. No restart needed.");
        System.out.println("Switch to the game and press INSERT to open the ClickGUI.");
        System.out.println("(Watch the game's log/console for [Kazhi] messages.)");
        pause();
    }

    private static VirtualMachineDescriptor waitForMinecraft(int timeoutSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (true) {
            VirtualMachineDescriptor found = findMinecraft();
            if (found != null) {
                return found;
            }
            if (System.currentTimeMillis() >= deadline) {
                return null;
            }
            Thread.sleep(2000);
        }
    }

    private static VirtualMachineDescriptor findMinecraft() {
        String self = ProcessHandle.current().pid() + "";
        for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
            if (vmd.id().equals(self)) {
                continue;
            }
            String display = vmd.displayName() == null ? "" : vmd.displayName().toLowerCase(Locale.ROOT);
            if (display.contains("kazhi-injector") || display.contains("gradle") || display.contains("jpackage")) {
                continue;
            }
            if (GAME_MATCHERS.stream().anyMatch(display::contains)) {
                return vmd;
            }
        }
        return null;
    }

    private static void pause() {
        System.out.println();
        System.out.println("Press Enter to close...");
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
    }

    private static void listProcesses() throws IOException {
        System.out.println("PID\tDisplay name");
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            String display = descriptor.displayName().toLowerCase(Locale.ROOT);
            boolean match = PROCESS_HINTS.stream().anyMatch(display::contains);
            if (match) {
                System.out.println(descriptor.id() + "\t" + descriptor.displayName());
            }
        }
        System.out.println("\nUse: kazhi-injector inject <pid>   (or run with no args to auto-inject)");
    }

    private static void inject(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: inject <pid> [--game-dir <path>]");
            return;
        }
        String pid = args[1];
        Path agentJar = JarLocator.findAgentJar();

        VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            vm.loadAgent(agentJar.toAbsolutePath().toString(), "");
            System.out.println("Agent loaded into PID " + pid + " (no restart needed).");
        } finally {
            vm.detach();
        }
    }

    private static void printUsage() {
        System.out.println("""
                Kazhi Injector v%s — NeoForge 1.21.8 (NetEase PC)

                Run with no arguments (or double-click the EXE) to AUTO-INJECT:
                """.formatted(VERSION) + """
                  finds the running Minecraft process and installs the client.

                Commands:
                  (none) / auto    Auto-find the running Minecraft and inject live
                  list             List likely Minecraft JVM processes
                  inject <pid>     Inject into a specific PID

                Injection is live: no mod, no mods/ folder, no restart.
                If attach fails on Java 21+, add JVM flag: -XX:+EnableDynamicAgentLoading
                """);
    }
}
