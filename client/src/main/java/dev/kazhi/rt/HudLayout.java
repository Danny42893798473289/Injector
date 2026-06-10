package dev.kazhi.rt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** HUD element positions. -1 on an axis means auto (right/bottom aligned). */
public final class HudLayout {
    public static int coordsX = 4;
    public static int coordsY = 4;
    public static int moduleListX = -1;
    public static int moduleListY = 4;
    public static int fpsX = 4;
    public static int fpsY = -1;
    public static int clockX = 4;
    public static int clockY = -1;
    public static int blockCountX = 4;
    public static int blockCountY = 72;
    public static int pingX = 4;
    public static int pingY = -1;

    public static float scale = 1.0F;
    public static boolean showBackground = true;
    public static boolean snapToGrid = true;
    public static int gridSize = 4;

    private HudLayout() {}

    public static int resolveX(int configured, GuiGraphics graphics, Minecraft mc, String text) {
        if (configured >= 0) {
            return configured;
        }
        return graphics.guiWidth() - mc.font.width(text) - 4;
    }

    public static int resolveY(int configured, GuiGraphics graphics, Minecraft mc, int lineIndex) {
        if (configured >= 0) {
            return configured + lineIndex * (mc.font.lineHeight + 2);
        }
        int base = graphics.guiHeight() - mc.font.lineHeight - 4;
        return base - lineIndex * (mc.font.lineHeight + 2);
    }
}
