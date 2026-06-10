package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FpsHudModule extends Module {
    private static FpsHudModule instance;

    public FpsHudModule() {
        super("FPS", "Show frames per second on the HUD", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        String text = mc.fpsString + " FPS";
        int x = HudLayout.resolveX(HudLayout.fpsX, graphics, mc, text);
        int y = HudLayout.resolveY(HudLayout.fpsY, graphics, mc, 0);
        HudElements.drawLabel(graphics, mc, text, x, y, 0xE0FFFFFF);
    }
}
