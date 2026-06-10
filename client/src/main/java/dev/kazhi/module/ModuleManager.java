package dev.kazhi.module;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.config.KazhiInput;
import dev.kazhi.module.impl.AutoSprintModule;
import dev.kazhi.module.impl.AutoToolModule;
import dev.kazhi.module.impl.AirPlaceModule;
import dev.kazhi.module.impl.BridgeModule;
import dev.kazhi.module.impl.ReachModule;
import dev.kazhi.module.impl.ReplenishModule;
import dev.kazhi.module.impl.TowerModule;
import dev.kazhi.module.impl.BlockCountHudModule;
import dev.kazhi.module.impl.ClientModule;
import dev.kazhi.module.impl.BoxFillModule;
import dev.kazhi.module.impl.ChunkBoundsModule;
import dev.kazhi.module.impl.ChunkGridModule;
import dev.kazhi.module.impl.ClockHudModule;
import dev.kazhi.module.impl.CoordsHudModule;
import dev.kazhi.module.impl.CrosshairModule;
import dev.kazhi.module.impl.FastBreakModule;
import dev.kazhi.module.impl.FastPlaceModule;
import dev.kazhi.module.impl.InstaMineModule;
import dev.kazhi.module.impl.NukerModule;
import dev.kazhi.module.impl.FlightModule;
import dev.kazhi.module.impl.FreecamModule;
import dev.kazhi.module.impl.FpsHudModule;
import dev.kazhi.module.impl.FullbrightModule;
import dev.kazhi.module.impl.HudEditModule;
import dev.kazhi.module.impl.JesusModule;
import dev.kazhi.module.impl.ModuleListModule;
import dev.kazhi.module.impl.MobEspModule;
import dev.kazhi.module.impl.NoFallModule;
import dev.kazhi.module.impl.NoJumpDelayModule;
import dev.kazhi.module.impl.NoFogModule;
import dev.kazhi.module.impl.NoHurtCamModule;
import dev.kazhi.module.impl.PingHudModule;
import dev.kazhi.module.impl.NoSlowModule;
import dev.kazhi.module.impl.NoSwingModule;
import dev.kazhi.module.impl.NoWeatherModule;
import dev.kazhi.module.impl.SafeWalkModule;
import dev.kazhi.module.impl.ScaffoldModule;
import dev.kazhi.module.impl.SpeedModule;
import dev.kazhi.module.impl.StepModule;
import dev.kazhi.module.impl.StorageEspModule;
import dev.kazhi.module.impl.TimerModule;
import dev.kazhi.module.impl.XRayModule;
import dev.kazhi.module.impl.ZoomModule;
import dev.kazhi.module.impl.stress.AnvilRenameStressTest;
import dev.kazhi.module.impl.stress.BlockPlaceBreakStressTest;
import dev.kazhi.module.impl.stress.BookEditStressTest;
import dev.kazhi.module.impl.stress.ChestStressTest;
import dev.kazhi.module.impl.stress.CreativeInventoryStressTest;
import dev.kazhi.module.impl.stress.InventoryStressTest;
import dev.kazhi.module.impl.stress.LecternStressTest;
import dev.kazhi.module.impl.stress.MultiContainerStressTest;
import dev.kazhi.module.impl.stress.PickBlockStressTest;
import dev.kazhi.module.impl.stress.PositionElytraStressTest;
import dev.kazhi.module.impl.stress.RecipeBookStressTest;
import dev.kazhi.module.impl.stress.ServerCrasherStressTest;
import dev.kazhi.module.impl.stress.ShulkerStressTest;
import dev.kazhi.module.impl.stress.SignStressTest;
import dev.kazhi.module.impl.stress.SurvivalDropStressTest;
import dev.kazhi.build.RegionClipboard;
import dev.kazhi.gui.GuiTheme;
import dev.kazhi.rt.HudLayout;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private static ModuleManager instance;

    private final Map<String, Module> modulesByName = new LinkedHashMap<>();
    private final List<Module> modules = new ArrayList<>();
    private boolean defaultsReady;
    private Map<String, Boolean> panicSnapshot;

    public static ModuleManager get() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    public void register(Module module) {
        modules.add(module);
        modulesByName.put(module.getName().toLowerCase(), module);
    }

    public void initDefaults() {
        if (defaultsReady) {
            return;
        }
        defaultsReady = true;
        register(new FlightModule());
        register(new NoFallModule());
        register(new NoJumpDelayModule());
        register(new AutoSprintModule());
        register(new StepModule());
        register(new SpeedModule());
        register(new SafeWalkModule());
        register(new JesusModule());
        register(new NoSlowModule());
        register(new XRayModule());
        register(new FullbrightModule());
        register(new ZoomModule());
        register(new NoHurtCamModule());
        register(new NoFogModule());
        register(new NoWeatherModule());
        register(new NoSwingModule());
        register(new ChunkBoundsModule());
        register(new ChunkGridModule());
        register(new StorageEspModule());
        register(new MobEspModule());
        register(new FreecamModule());
        register(new CoordsHudModule());
        register(new ModuleListModule());
        register(new FpsHudModule());
        register(new PingHudModule());
        register(new ClockHudModule());
        register(new BlockCountHudModule());
        register(new CrosshairModule());
        register(new HudEditModule());
        register(new ClientModule());
        register(new BoxFillModule());
        register(new FastPlaceModule());
        register(new FastBreakModule());
        register(new InstaMineModule());
        register(new NukerModule());
        register(new AutoToolModule());
        register(new ScaffoldModule());
        register(new AirPlaceModule());
        register(new TowerModule());
        register(new BridgeModule());
        register(new ReplenishModule());
        register(new ReachModule());
        register(new TimerModule());
        register(new ShulkerStressTest());
        register(new InventoryStressTest());
        register(new ChestStressTest());
        register(new BookEditStressTest());
        register(new LecternStressTest());
        register(new SignStressTest());
        register(new BlockPlaceBreakStressTest());
        register(new PositionElytraStressTest());
        register(new AnvilRenameStressTest());
        register(new MultiContainerStressTest());
        register(new SurvivalDropStressTest());
        register(new CreativeInventoryStressTest());
        register(new PickBlockStressTest());
        register(new RecipeBookStressTest());
        register(new ServerCrasherStressTest());
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    public Optional<Module> get(String name) {
        return Optional.ofNullable(modulesByName.get(name.toLowerCase()));
    }

    public void tickAll() {
        ClockHudModule.tickHud();
        BlockCountHudModule.tickHud();
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void disableAll() {
        for (Module module : modules) {
            module.setEnabled(false);
        }
    }

    public boolean hasPanicSnapshot() {
        return panicSnapshot != null && !panicSnapshot.isEmpty();
    }

    public void snapshotForPanic() {
        panicSnapshot = new HashMap<>();
        for (Module module : modules) {
            panicSnapshot.put(module.getName(), module.isEnabled());
        }
    }

    public void restoreFromPanic() {
        if (panicSnapshot == null) {
            return;
        }
        for (Module module : modules) {
            Boolean was = panicSnapshot.get(module.getName());
            if (was != null) {
                module.setEnabled(was);
            }
        }
        panicSnapshot = null;
    }

    public void loadFromConfig(KazhiConfig config) {
        KazhiInput.apply(config);

        for (Module module : modules) {
            module.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
        }

        Map<String, KazhiConfig.ModuleEntry> entries = new HashMap<>();
        for (KazhiConfig.ModuleEntry entry : config.modules) {
            if (entry.name != null) {
                entries.put(entry.name.toLowerCase(Locale.ROOT), entry);
            }
        }
        for (Module module : modules) {
            KazhiConfig.ModuleEntry entry = entries.get(module.getName().toLowerCase(Locale.ROOT));
            if (entry == null) {
                continue;
            }
            module.setKeyCode(entry.keyCode == 0 ? GLFW.GLFW_KEY_UNKNOWN : entry.keyCode);
            module.setEnabled(entry.enabled);
        }
        XRayModule.applyExtraBlocks(config.xrayExtraBlocks);
        BoxFillModule.centerX = config.boxFillX;
        BoxFillModule.centerY = config.boxFillY;
        BoxFillModule.centerZ = config.boxFillZ;
        BoxFillModule.radius = config.boxFillRadius;
        BoxFillModule.hollow = config.boxFillHollow;
        BoxFillModule.airOnly = config.boxFillAirOnly;
        BoxFillModule.replaceMode = config.boxFillReplaceMode;
        BoxFillModule.selectionOutline = config.boxFillSelectionOutline;
        BoxFillModule.layerMode = config.boxFillLayerMode;
        BoxFillModule.layerMinY = config.boxFillLayerMinY;
        BoxFillModule.layerMaxY = config.boxFillLayerMaxY;
        BoxFillModule.liquidFillMode = config.boxFillLiquidMode;
        RegionClipboard.pasteRotation = config.pasteRotation;
        RegionClipboard.mirrorX = config.pasteMirrorX;
        RegionClipboard.mirrorZ = config.pasteMirrorZ;
        BoxFillModule.corner1X = config.regionX1;
        BoxFillModule.corner1Y = config.regionY1;
        BoxFillModule.corner1Z = config.regionZ1;
        BoxFillModule.corner2X = config.regionX2;
        BoxFillModule.corner2Y = config.regionY2;
        BoxFillModule.corner2Z = config.regionZ2;
        TimerModule.speed = config.timerSpeed;
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
    }

    public KazhiConfig saveToConfig() {
        KazhiConfig config = new KazhiConfig();
        KazhiInput.writeTo(config);
        config.zoomMultiplier = ZoomModule.zoomMultiplier;
        config.fullbrightStrength = FullbrightModule.strength;
        config.timerSpeed = TimerModule.speed;
        XRayModule.saveOreFlagsTo(config);

        for (Module module : modules) {
            KazhiConfig.ModuleEntry entry = new KazhiConfig.ModuleEntry();
            entry.name = module.getName();
            entry.enabled = module.isEnabled();
            entry.keyCode = module.getKeyCode();
            config.modules.add(entry);
        }
        config.xrayExtraBlocks = XRayModule.getExtraBlockIds();
        config.boxFillX = BoxFillModule.centerX;
        config.boxFillY = BoxFillModule.centerY;
        config.boxFillZ = BoxFillModule.centerZ;
        config.boxFillRadius = BoxFillModule.radius;
        config.boxFillHollow = BoxFillModule.hollow;
        config.boxFillAirOnly = BoxFillModule.airOnly;
        config.boxFillReplaceMode = BoxFillModule.replaceMode;
        config.boxFillSelectionOutline = BoxFillModule.selectionOutline;
        config.boxFillLayerMode = BoxFillModule.layerMode;
        config.boxFillLayerMinY = BoxFillModule.layerMinY;
        config.boxFillLayerMaxY = BoxFillModule.layerMaxY;
        config.boxFillLiquidMode = BoxFillModule.liquidFillMode;
        config.pasteRotation = RegionClipboard.pasteRotation;
        config.pasteMirrorX = RegionClipboard.mirrorX;
        config.pasteMirrorZ = RegionClipboard.mirrorZ;
        config.regionX1 = BoxFillModule.corner1X;
        config.regionY1 = BoxFillModule.corner1Y;
        config.regionZ1 = BoxFillModule.corner1Z;
        config.regionX2 = BoxFillModule.corner2X;
        config.regionY2 = BoxFillModule.corner2Y;
        config.regionZ2 = BoxFillModule.corner2Z;
        config.hudCoordsX = HudLayout.coordsX;
        config.hudCoordsY = HudLayout.coordsY;
        config.hudModuleListX = HudLayout.moduleListX;
        config.hudModuleListY = HudLayout.moduleListY;
        config.hudFpsX = HudLayout.fpsX;
        config.hudFpsY = HudLayout.fpsY;
        config.hudClockX = HudLayout.clockX;
        config.hudClockY = HudLayout.clockY;
        config.hudBlockCountX = HudLayout.blockCountX;
        config.hudBlockCountY = HudLayout.blockCountY;
        config.hudPingX = HudLayout.pingX;
        config.hudPingY = HudLayout.pingY;
        config.hudScale = HudLayout.scale;
        config.hudShowBackground = HudLayout.showBackground;
        config.hudSnapToGrid = HudLayout.snapToGrid;
        config.clickGuiTheme = GuiTheme.current().ordinal();
        return config;
    }
}
