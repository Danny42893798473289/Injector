package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CrosshairModule extends Module {
    private static CrosshairModule instance;

    public CrosshairModule() {
        super("Crosshair", "Custom crosshair overlay", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        if (mc.options.hideGui || mc.screen != null) {
            return;
        }
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        int color = 0xFFFFFFFF;
        int gap = 3;
        int len = 6;
        graphics.fill(cx - gap - len, cy, cx - gap, cy + 1, color);
        graphics.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, color);
        graphics.fill(cx, cy - gap - len, cx + 1, cy - gap, color);
        graphics.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, color);
        graphics.fill(cx, cy, cx + 1, cy + 1, 0x80FFFFFF);
    }
}
