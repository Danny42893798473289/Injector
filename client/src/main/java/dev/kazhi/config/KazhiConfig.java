package dev.kazhi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.kazhi.module.ModuleManager;
import dev.kazhi.module.impl.FullbrightModule;
import dev.kazhi.module.impl.XRayModule;
import dev.kazhi.module.impl.TimerModule;
import dev.kazhi.module.impl.ZoomModule;
import dev.kazhi.gui.GuiTheme;
import dev.kazhi.rt.HudLayout;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KazhiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path cachedPath;
    public static String activeProfile = "default";

    public int configVersion = 4;

    /** Runtime toggle; synced from {@link #boxFillPreviewSetting} on load/save. */
    public static boolean boxFillPreview = true;

    public List<ModuleEntry> modules = new ArrayList<>();
    public List<String> xrayExtraBlocks = new ArrayList<>();

    public int menuKeyCode = KazhiKeys.DEFAULT_MENU;
    public int zoomKeyCode = KazhiKeys.DEFAULT_ZOOM;
    public int panicKeyCode = KazhiKeys.DEFAULT_PANIC;
    public int pos1KeyCode = KazhiKeys.DEFAULT_POS1;
    public int pos2KeyCode = KazhiKeys.DEFAULT_POS2;

    public float zoomMultiplier = 0.35F;
    public float fullbrightStrength = 1.0F;
    public float timerSpeed = 1.0F;

    public boolean xrayCoal = true;
    public boolean xrayIron = true;
    public boolean xrayCopper = true;
    public boolean xrayGold = true;
    public boolean xrayRedstone = true;
    public boolean xrayLapis = true;
    public boolean xrayDiamond = true;
    public boolean xrayEmerald = true;
    public boolean xrayAncientDebris = true;
    public boolean xrayNetherGold = true;
    public boolean xrayNetherQuartz = true;

    @SerializedName("boxFillPreview")
    public boolean boxFillPreviewSetting = true;

    public int boxFillX;
    public int boxFillY = 64;
    public int boxFillZ;
    public int boxFillRadius = 1;
    public boolean boxFillHollow;
    public boolean boxFillAirOnly = true;
    public boolean boxFillReplaceMode;
    public boolean boxFillSelectionOutline = true;
    public boolean boxFillLayerMode;
    public int boxFillLayerMinY = -1;
    public int boxFillLayerMaxY = -1;
    public int boxFillLiquidMode;
    public int pasteRotation;
    public boolean pasteMirrorX;
    public boolean pasteMirrorZ;
    public int hudCoordsX = 4;
    public int hudCoordsY = 4;
    public int hudModuleListX = -1;
    public int hudModuleListY = 4;
    public int hudFpsX = 4;
    public int hudFpsY = -1;
    public int hudClockX = 4;
    public int hudClockY = -1;
    public int hudBlockCountX = 4;
    public int hudBlockCountY = 72;
    public int hudPingX = 4;
    public int hudPingY = -1;
    public float hudScale = 1.0F;
    public boolean hudShowBackground = true;
    public boolean hudSnapToGrid = true;
    public int regionX1;
    public int regionY1 = 64;
    public int regionZ1;
    public int regionX2;
    public int regionY2 = 64;
    public int regionZ2;
    public int clickGuiTheme;

    public static class ModuleEntry {
        public String name;
        public boolean enabled;
        public int keyCode;
    }

    public static Path getConfigPath() {
        if (cachedPath == null) {
            cachedPath = resolveSavePath();
        }
        return cachedPath;
    }

    private static Path resolveGameConfigPath() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return mc.gameDirectory.toPath()
                        .resolve("config").resolve("kazhi").resolve(profileFileName(activeProfile));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String profileFileName(String profile) {
        if (profile == null || profile.isBlank() || "default".equals(profile)) {
            return "settings.json";
        }
        return "settings-" + profile + ".json";
    }

    private static Path fallbackConfigPath() {
        return Path.of(System.getProperty("user.home"), ".kazhi", profileFileName(activeProfile));
    }

    public static void switchProfile(String profile) {
        save();
        activeProfile = profile == null || profile.isBlank() ? "default" : profile;
        cachedPath = null;
        load();
    }

    private static Path resolveSavePath() {
        Path game = resolveGameConfigPath();
        return game != null ? game : fallbackConfigPath();
    }

    private static Path resolveLoadPath() {
        Path game = resolveGameConfigPath();
        if (game != null && Files.exists(game)) {
            return game;
        }
        Path fallback = fallbackConfigPath();
        if (Files.exists(fallback)) {
            return fallback;
        }
        return game != null ? game : fallback;
    }

    public static void load() {
        Path path = resolveLoadPath();
        cachedPath = path;

        KazhiConfig config = new KazhiConfig();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                KazhiConfig loaded = GSON.fromJson(reader, KazhiConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
                KazhiLog.log("Loaded config from " + path);
            } catch (IOException e) {
                KazhiLog.error("Failed to load config from " + path, e);
            }
        } else {
            KazhiLog.log("No config at " + path + " — creating defaults");
            applyFrom(config);
            save();
            return;
        }
        applyFrom(config);
    }

    private static void applyFrom(KazhiConfig config) {
        ModuleManager.get().loadFromConfig(config);
        ZoomModule.zoomMultiplier = clamp(config.zoomMultiplier, 0.1F, 1.0F);
        FullbrightModule.strength = clamp(config.fullbrightStrength, 0.0F, 1.0F);
        TimerModule.speed = clamp(config.timerSpeed, 0.25F, 5.0F);
        boxFillPreview = config.boxFillPreviewSetting;
        GuiTheme.setCurrent(config.clickGuiTheme);
        XRayModule.applyOreFlags(config);
        if (config.configVersion >= 2) {
            HudLayout.coordsX = config.hudCoordsX;
            HudLayout.coordsY = config.hudCoordsY;
            HudLayout.moduleListX = config.hudModuleListX;
            HudLayout.moduleListY = config.hudModuleListY;
            HudLayout.fpsX = config.hudFpsX;
            HudLayout.fpsY = config.hudFpsY;
            HudLayout.clockX = config.hudClockX;
            HudLayout.clockY = config.hudClockY;
            HudLayout.blockCountX = config.hudBlockCountX;
            HudLayout.blockCountY = config.hudBlockCountY;
            HudLayout.pingX = config.hudPingX;
            HudLayout.pingY = config.hudPingY;
            if (config.configVersion >= 4) {
                HudLayout.scale = clamp(config.hudScale, 0.7F, 1.5F);
                HudLayout.showBackground = config.hudShowBackground;
                HudLayout.snapToGrid = config.hudSnapToGrid;
            }
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            KazhiConfig config = ModuleManager.get().saveToConfig();
            config.boxFillPreviewSetting = boxFillPreview;
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
            KazhiLog.log("Saved config to " + path);
        } catch (IOException e) {
            KazhiLog.error("Failed to save config to " + path, e);
        }
    }

    public static void registerShutdownSave() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save();
            } catch (Throwable ignored) {
            }
        }, "kazhi-config-save"));
    }
}
