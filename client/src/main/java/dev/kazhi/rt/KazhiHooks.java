package dev.kazhi.rt;

import dev.kazhi.build.BoxFillPreview;
import dev.kazhi.build.PacketFill;
import dev.kazhi.config.KazhiConfig;
import dev.kazhi.config.KazhiInput;
import dev.kazhi.gui.ClickGuiScreen;
import dev.kazhi.module.Module;
import dev.kazhi.module.ModuleManager;
import dev.kazhi.module.impl.BoxFillModule;
import dev.kazhi.module.impl.FullbrightModule;
import dev.kazhi.module.impl.XRayModule;
import dev.kazhi.module.impl.HudEditModule;
import dev.kazhi.module.impl.NoFallModule;
import dev.kazhi.module.impl.ZoomModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public final class KazhiHooks {
    private static final String VERSION = "1.3.1";

    private static boolean initialized;
    private static final Set<Integer> heldKeys = new HashSet<>();
    private static boolean guiKeyHeld;
    private static boolean panicKeyHeld;
    private static int errorCount;
    static int hudErrorCount;

    private KazhiHooks() {}

    public static String getVersion() {
        return VERSION;
    }

    public static void onClientTick() {
        try {
            tick();
        } catch (Throwable t) {
            if (errorCount++ < 5) {
                KazhiLog.error("hook tick error", t);
            }
        }
    }

    public static void onHudRender(GuiGraphics graphics, DeltaTracker tick) {
        HudRenderer.render(graphics, tick.getGameTimeDeltaPartialTick(false));
    }

    public static void onWorldRender() {
        WorldRenderer.render();
    }

    public static void onPlayerTickPost() {
        try {
            NoFallModule.applyPost();
        } catch (Throwable t) {
            if (errorCount++ < 5) {
                KazhiLog.error("player tick post hook error", t);
            }
        }
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }

        if (!initialized) {
            initialized = true;
            ModuleManager.get().initDefaults();
            KazhiConfig.load();
            KazhiConfig.registerShutdownSave();
            KazhiLog.log("Runtime client active. Press " + keyLabel(KazhiInput.menuKey) + " for the menu. Modules: "
                    + ModuleManager.get().getModules().size() + ". Config: " + KazhiConfig.getConfigPath());
        }

        long window = mc.getWindow().getWindow();

        boolean insert = GLFW.glfwGetKey(window, KazhiInput.menuKey) == GLFW.GLFW_PRESS;
        if (insert && !guiKeyHeld && mc.screen == null) {
            try {
                mc.setScreen(new ClickGuiScreen());
            } catch (Throwable t) {
                KazhiLog.error("Failed to open ClickGUI", t);
            }
        }
        guiKeyHeld = insert;

        ZoomModule.zoomKeyHeld = GLFW.glfwGetKey(window, KazhiInput.zoomKey) == GLFW.GLFW_PRESS;

        boolean panic = GLFW.glfwGetKey(window, KazhiInput.panicKey) == GLFW.GLFW_PRESS;
        if (panic && !panicKeyHeld) {
            if (ModuleManager.get().hasPanicSnapshot()) {
                ModuleManager.get().restoreFromPanic();
                KazhiLog.log("Panic restore: modules reverted.");
            } else {
                ModuleManager.get().snapshotForPanic();
                ModuleManager.get().disableAll();
                KazhiLog.log("Panic: all modules disabled (press again to restore).");
            }
            KazhiConfig.save();
        }
        panicKeyHeld = panic;

        HudEditModule.handleMouse(mc);

        if (mc.screen == null) {
            handleEdgeKey(window, KazhiInput.pos1Key, () -> {
                BoxFillModule.setPos1FromCrosshair();
                KazhiConfig.save();
            });
            handleEdgeKey(window, KazhiInput.pos2Key, () -> {
                BoxFillModule.setPos2FromCrosshair();
                KazhiConfig.save();
            });
            handleEdgeKey(window, GLFW.GLFW_KEY_U, BoxFillModule::undoLastFill);

            for (Module module : ModuleManager.get().getModules()) {
                int key = module.getKeyCode();
                if (key == 0 || key == GLFW.GLFW_KEY_UNKNOWN) {
                    continue;
                }
                boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
                if (down && !heldKeys.contains(key)) {
                    module.toggle();
                    KazhiConfig.save();
                }
                if (down) {
                    heldKeys.add(key);
                } else {
                    heldKeys.remove(key);
                }
            }
        }

        if (KazhiConfig.boxFillPreview) {
            BoxFillPreview.tick();
        }

        ModuleManager.get().tickAll();
        PacketFill.tick();
    }

    private static void handleEdgeKey(long window, int key, Runnable action) {
        if (key == 0 || key == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }
        boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        if (down && !heldKeys.contains(key)) {
            action.run();
        }
        if (down) {
            heldKeys.add(key);
        } else {
            heldKeys.remove(key);
        }
    }

    private static String keyLabel(int keyCode) {
        return dev.kazhi.config.KazhiKeys.name(keyCode);
    }

    public static float fovMul() {
        try {
            return ZoomModule.getFovMultiplier();
        } catch (Throwable t) {
            return 1.0F;
        }
    }

    public static float applyFullbright(float brightness) {
        try {
            float strength = FullbrightModule.getStrength();
            if (strength <= 0.0F) {
                return brightness;
            }
            return brightness + (1.0F - brightness) * strength;
        } catch (Throwable t) {
            return brightness;
        }
    }

    public static boolean noHurtCam() {
        try {
            return dev.kazhi.module.impl.NoHurtCamModule.isActive();
        } catch (Throwable t) {
            return false;
        }
    }

    public static int xrayOverride(BlockState state) {
        try {
            if (!XRayModule.isActive()) {
                return -1;
            }
            return XRayModule.shouldXRay(state) ? 1 : 0;
        } catch (Throwable t) {
            return -1;
        }
    }
}
