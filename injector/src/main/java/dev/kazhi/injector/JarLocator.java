package dev.kazhi.injector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class JarLocator {
    private JarLocator() {}

    public static Path findAgentJar() throws IOException {
        return findJar("kazhi-agent");
    }

    public static Path findClientJar() throws IOException {
        return findJar("kazhi-client");
    }

    private static Path findJar(String prefix) throws IOException {
        Path selfDir = getSelfDirectory();
        try (Stream<Path> files = Files.list(selfDir)) {
            Path found = files
                    .filter(p -> p.getFileName().toString().startsWith(prefix) && p.toString().endsWith(".jar"))
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                return found;
            }
        }
        Path buildLibs = selfDir.resolve("..").resolve("agent").resolve("build").resolve("libs").normalize();
        if (prefix.contains("client")) {
            buildLibs = selfDir.resolve("..").resolve("client").resolve("build").resolve("libs").normalize();
        }
        if (Files.isDirectory(buildLibs)) {
            try (Stream<Path> files = Files.list(buildLibs)) {
                Path found = files
                        .filter(p -> p.getFileName().toString().startsWith(prefix) && !p.toString().contains("-sources"))
                        .findFirst()
                        .orElse(null);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new IOException("Could not find " + prefix + ".jar near injector or in build/libs");
    }

    private static Path getSelfDirectory() {
        try {
            Path code = Path.of(InjectorMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(code)) {
                return code.getParent();
            }
            return code;
        } catch (URISyntaxException e) {
            return Path.of(".").toAbsolutePath();
        }
    }
}
