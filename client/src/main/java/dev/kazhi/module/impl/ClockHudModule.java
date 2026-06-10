package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ClockHudModule extends Module {
    private static ClockHudModule instance;
    private static long lastTickMs;
    private static float smoothedTps = 20.0F;

    public ClockHudModule() {
        super("ClockHUD", "World time and tick rate overlay", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void tickHud() {
        if (!isActive()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastTickMs > 0) {
            float instant = 1000.0F / Math.max(1, now - lastTickMs);
            smoothedTps = smoothedTps * 0.9F + instant * 0.1F;
        }
        lastTickMs = now;
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        long time = mc.level.getDayTime() % 24000L;
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);
        String text = String.format("Time %02d:%02d  |  TPS %.1f", hours, minutes, smoothedTps);
        int x = HudLayout.resolveX(HudLayout.clockX, graphics, mc, text);
        int y = HudLayout.resolveY(HudLayout.clockY, graphics, mc, 0);
        HudElements.drawLabel(graphics, mc, text, x, y, 0xE0FFFFFF);
    }
}
