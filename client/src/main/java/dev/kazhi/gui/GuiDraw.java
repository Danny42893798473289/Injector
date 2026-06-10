package dev.kazhi.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class GuiDraw {
    private GuiDraw() {}

    public static void panel(GuiGraphics g, int x, int y, int w, int h, GuiTheme theme) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, theme.panelBorder);
        g.fill(x, y, x + w, y + h, theme.panelBg);
    }

    public static void headerBar(GuiGraphics g, int x, int y, int w, int h, GuiTheme theme) {
        g.fill(x, y, x + w, y + h, theme.headerBg);
        g.fill(x, y + h - 1, x + w, y + h, theme.accentDim);
    }

    public static void accentLine(GuiGraphics g, int x, int y, int h, int color) {
        g.fill(x, y, x + 2, y + h, color);
    }

    public static void togglePill(GuiGraphics g, Font font, int x, int y, int w, int h, boolean on, GuiTheme theme) {
        int bg = on ? withAlpha(theme.enabled, 0x55) : theme.rowHover;
        int border = on ? theme.enabled : theme.panelBorder;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        String label = on ? "ON" : "OFF";
        int tw = font.width(label);
        g.drawString(font, label, x + (w - tw) / 2, y + (h - 8) / 2, on ? theme.enabled : theme.disabled);
    }

    public static void keyChip(GuiGraphics g, Font font, int x, int y, int w, int h, String label, boolean binding, GuiTheme theme) {
        int bg = binding ? withAlpha(theme.accent, 0x44) : theme.keyBg;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y + h - 1, x + w, y + h, theme.accentDim);
        int tw = font.width(label);
        g.drawString(font, label, x + Math.max(4, (w - tw) / 2), y + (h - 8) / 2, binding ? theme.accent : theme.textDim);
    }

    public static void scrollbar(GuiGraphics g, int x, int y, int h, int total, int visible, int offset, GuiTheme theme) {
        if (total <= visible) {
            return;
        }
        int trackH = h - 4;
        g.fill(x, y + 2, x + 3, y + 2 + trackH, theme.rowHover);
        float ratio = (float) visible / total;
        int thumbH = Math.max(12, (int) (trackH * ratio));
        float scrollRatio = (float) offset / Math.max(1, total - visible);
        int thumbY = y + 2 + (int) ((trackH - thumbH) * scrollRatio);
        g.fill(x, thumbY, x + 3, thumbY + thumbH, theme.accent);
    }

    public static void themeDot(GuiGraphics g, int cx, int cy, int r, GuiTheme theme, boolean selected) {
        g.fill(cx - r - 1, cy - r - 1, cx + r + 1, cy + r + 1, selected ? 0xFFFFFFFF : theme.panelBorder);
        g.fill(cx - r, cy - r, cx + r, cy + r, theme.accent);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
