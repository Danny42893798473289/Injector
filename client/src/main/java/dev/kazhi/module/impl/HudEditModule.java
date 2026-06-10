package dev.kazhi.module.impl;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.gui.HudEditScreen;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class HudEditModule extends Module implements HasSettings {
    private static HudEditModule instance;
    private static HudElements.Entry dragTarget;

    public HudEditModule() {
        super("HudEdit", "Drag HUD on-screen or open layout editor (RMB)", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new HudEditScreen(parent);
    }

    public static void handleMouse(Minecraft mc) {
        if (!isActive() || mc.screen != null) {
            dragTarget = null;
            return;
        }
        long window = mc.getWindow().getWindow();
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        int mx = (int) (x[0] * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
        int my = (int) (y[0] * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
        boolean down = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (down && dragTarget == null) {
            for (int i = HudElements.all().size() - 1; i >= 0; i--) {
                HudElements.Entry entry = HudElements.all().get(i);
                String sample = sampleFor(entry);
                if (entry.containsScreen(mc, mx, my, sample)) {
                    dragTarget = entry;
                    if (entry.getX().getAsInt() < 0) {
                        entry.setX().accept(entry.screenX(mc, sample));
                    }
                    if (entry.getY().getAsInt() < 0) {
                        entry.setY().accept(entry.screenY(mc, 0));
                    }
                    break;
                }
            }
        }

        if (dragTarget != null && down) {
            dragTarget.moveTo(mx, my);
            return;
        }

        if (!down && dragTarget != null) {
            dragTarget = null;
            KazhiConfig.save();
        }
    }

    private static String sampleFor(HudElements.Entry entry) {
        if (entry == HudElements.FPS) {
            return "120 FPS";
        }
        if (entry == HudElements.PING) {
            return "Ping: 42ms";
        }
        if (entry == HudElements.CLOCK) {
            return "Time 12:00  |  TPS 20.0";
        }
        if (entry == HudElements.BLOCK_COUNT) {
            return "Selection blocks: 0";
        }
        if (entry == HudElements.MODULE_LIST) {
            return "ModuleList";
        }
        return "XYZ 0 64 0  |  overworld";
    }
}
