package dev.kazhi.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Performs true runtime injection into a live Minecraft (NeoForge) JVM:
 *   1. Finds the game class loader.
 *   2. Defines our payload classes (compiled against Minecraft) directly into it.
 *   3. Rewrites already-loaded Minecraft classes via retransformClasses.
 * No mod, no mods/ folder, no restart.
 */
public final class RuntimeInjector {

    private static final String[] PAYLOAD_PREFIXES = {
            "dev/kazhi/rt/", "dev/kazhi/module/", "dev/kazhi/gui/", "dev/kazhi/config/", "dev/kazhi/build/"
    };

    private static final String[] TARGET_CLASSES = {
            "net.minecraft.client.renderer.GameRenderer",
            "net.minecraft.client.renderer.LightTexture",
            "net.minecraft.world.level.block.Block",
            "net.minecraft.client.Minecraft",
            "net.minecraft.client.gui.Gui",
            "net.minecraft.client.Camera"
    };

    private static boolean injected;

    private RuntimeInjector() {}

    public static synchronized void inject(Instrumentation inst, String args) {
        AgentLog.log("agentmain called. injected=" + injected);
        if (injected) {
            AgentLog.log("Already injected; ignoring.");
            return;
        }
        try {
            Path payloadJar = resolvePayloadJar();
            if (payloadJar == null) {
                AgentLog.error("Could not find kazhi-client*.jar next to the agent.", null);
                return;
            }
            AgentLog.log("Payload: " + payloadJar);

            ClassLoader gameLoader = findGameClassLoader();
            if (gameLoader == null) {
                AgentLog.error("Could not locate the Minecraft class loader. Is the game at the main menu?", null);
                return;
            }
            AgentLog.log("Game class loader: " + gameLoader.getClass().getName());

            openJavaLang(inst);
            openGameModules(inst, gameLoader);

            Map<String, byte[]> classes = readPayloadClasses(payloadJar);
            AgentLog.log("Read " + classes.size() + " payload classes from jar.");
            defineClasses(gameLoader, classes);

            // Confirm our entry point is now visible to the game loader.
            Class.forName("dev.kazhi.rt.KazhiHooks", false, gameLoader);
            AgentLog.log("KazhiHooks resolved in game loader.");

            inst.addTransformer(new KazhiTransformer(), true);

            List<Class<?>> targets = new ArrayList<>();
            for (String name : TARGET_CLASSES) {
                try {
                    targets.add(Class.forName(name, false, gameLoader));
                } catch (Throwable t) {
                    AgentLog.error("Target not loaded yet: " + name, t);
                }
            }
            if (!targets.isEmpty()) {
                inst.retransformClasses(targets.toArray(new Class<?>[0]));
                AgentLog.log("Retransformed " + targets.size() + " classes.");
            }

            injected = true;
            AgentLog.log("Injection complete. Press the menu key (default INSERT) in-game.");
        } catch (Throwable t) {
            AgentLog.error("Injection failed", t);
        }
    }

    private static void openJavaLang(Instrumentation inst) {
        Module base = ClassLoader.class.getModule();
        Module self = RuntimeInjector.class.getModule();
        Map<String, Set<Module>> opens = Map.of("java.lang", Set.of(self));
        inst.redefineModule(base, Set.of(), Map.of(), opens, Set.of(), Map.of());
    }

    /**
     * Our payload classes live in the unnamed module of the game class loader. To let
     * them extend Screen and call Minecraft APIs, export+open the game's module packages
     * to our module. Without this, linking the injected classes throws IllegalAccessError.
     */
    private static void openGameModules(Instrumentation inst, ClassLoader loader) {
        Module self = RuntimeInjector.class.getModule();
        String[] probes = {
                "net.minecraft.client.Minecraft",
                "net.minecraft.client.gui.screens.Screen",
                "net.minecraft.client.gui.GuiGraphics",
                "net.minecraft.world.level.block.Block",
                "net.minecraft.network.chat.Component",
                "net.minecraft.client.DeltaTracker",
                "net.minecraft.client.Camera",
                "net.minecraft.client.renderer.ShapeRenderer"
        };
        Set<Module> done = new HashSet<>();
        for (String cn : probes) {
            try {
                Module m = Class.forName(cn, false, loader).getModule();
                if (m == null || !m.isNamed() || !done.add(m)) {
                    continue;
                }
                Map<String, Set<Module>> all = new java.util.HashMap<>();
                for (String pkg : m.getPackages()) {
                    all.put(pkg, Set.of(self));
                }
                inst.redefineModule(m, Set.of(), all, all, Set.of(), Map.of());
                AgentLog.log("Opened module '" + m.getName() + "' ("
                        + m.getPackages().size() + " packages) to the payload.");
            } catch (Throwable t) {
                AgentLog.error("Could not open module for " + cn, t);
            }
        }
    }

    @SuppressWarnings("removal")
    private static void defineClasses(ClassLoader loader, Map<String, byte[]> classes) throws Exception {
        Method define = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class);
        define.setAccessible(true);

        Set<String> defined = new HashSet<>();
        boolean progress = true;
        while (progress && defined.size() < classes.size()) {
            progress = false;
            for (Map.Entry<String, byte[]> e : classes.entrySet()) {
                String name = e.getKey();
                if (defined.contains(name)) {
                    continue;
                }
                byte[] bytes = e.getValue();
                try {
                    define.invoke(loader, name, bytes, 0, bytes.length);
                    defined.add(name);
                    progress = true;
                } catch (Throwable t) {
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    String msg = String.valueOf(cause.getMessage());
                    if (msg.contains("duplicate")) {
                        // Already present (e.g. a previous attach); treat as done.
                        defined.add(name);
                        progress = true;
                    }
                    // Otherwise (missing superclass) leave it for a later pass.
                }
            }
        }
        if (defined.size() < classes.size()) {
            for (String name : classes.keySet()) {
                if (!defined.contains(name)) {
                    AgentLog.error("Could not define: " + name, null);
                }
            }
        }
        AgentLog.log("Defined " + defined.size() + "/" + classes.size() + " payload classes.");
    }

    private static Map<String, byte[]> readPayloadClasses(Path jar) throws Exception {
        Map<String, byte[]> out = new LinkedHashMap<>();
        try (JarFile jf = new JarFile(jar.toFile())) {
            var entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String n = entry.getName();
                if (!n.endsWith(".class")) {
                    continue;
                }
                boolean match = false;
                for (String p : PAYLOAD_PREFIXES) {
                    if (n.startsWith(p)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    continue;
                }
                String binaryName = n.substring(0, n.length() - 6).replace('/', '.');
                try (var in = jf.getInputStream(entry)) {
                    out.put(binaryName, in.readAllBytes());
                }
            }
        }
        return out;
    }

    private static ClassLoader findGameClassLoader() {
        // Prefer the loader of an already-loaded Minecraft class via a known thread.
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            ClassLoader loader = thread.getContextClassLoader();
            if (canLoad(loader, "net.minecraft.client.Minecraft")) {
                return loader;
            }
        }
        // Fallback: scan all loaded classes.
        return null;
    }

    private static boolean canLoad(ClassLoader loader, String className) {
        if (loader == null) {
            return false;
        }
        try {
            Class.forName(className, false, loader);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Path resolvePayloadJar() {
        try {
            URL agentUrl = KazhiAgent.class.getProtectionDomain().getCodeSource().getLocation();
            if (agentUrl == null) {
                return null;
            }
            Path dir = Path.of(agentUrl.toURI()).getParent();
            if (dir == null) {
                return null;
            }
            try (var stream = Files.list(dir)) {
                return stream
                        .filter(p -> {
                            String fn = p.getFileName().toString();
                            return fn.startsWith("kazhi-client") && fn.endsWith(".jar") && !fn.contains("-sources");
                        })
                        .findFirst()
                        .orElse(null);
            }
        } catch (Throwable t) {
            return null;
        }
    }
}
