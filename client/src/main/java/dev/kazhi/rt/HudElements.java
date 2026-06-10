package dev.kazhi.rt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Registered HUD widgets for layout editing and rendering helpers. */
public final class HudElements {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static final Entry COORDS = register("Coords", () -> HudLayout.coordsX, () -> HudLayout.coordsY,
            v -> HudLayout.coordsX = v, v -> HudLayout.coordsY = v, mc -> 280, mc -> 36);
    public static final Entry CLOCK = register("Clock", () -> HudLayout.clockX, () -> HudLayout.clockY,
            v -> HudLayout.clockX = v, v -> HudLayout.clockY = v, mc -> 180, mc -> 12);
    public static final Entry BLOCK_COUNT = register("Block count", () -> HudLayout.blockCountX, () -> HudLayout.blockCountY,
            v -> HudLayout.blockCountX = v, v -> HudLayout.blockCountY = v, mc -> 160, mc -> 12);
    public static final Entry FPS = register("FPS", () -> HudLayout.fpsX, () -> HudLayout.fpsY,
            v -> HudLayout.fpsX = v, v -> HudLayout.fpsY = v, mc -> 64, mc -> 12);
    public static final Entry PING = register("Ping", () -> HudLayout.pingX, () -> HudLayout.pingY,
            v -> HudLayout.pingX = v, v -> HudLayout.pingY = v, mc -> 80, mc -> 12);
    public static final Entry MODULE_LIST = register("Module list", () -> HudLayout.moduleListX, () -> HudLayout.moduleListY,
            v -> HudLayout.moduleListX = v, v -> HudLayout.moduleListY = v, mc -> 100, mc -> 80);

    private HudElements() {}

    public static List<Entry> all() {
        return ENTRIES;
    }

    private static Entry register(
            String label,
            IntSupplier getX,
            IntSupplier getY,
            IntConsumer setX,
            IntConsumer setY,
            java.util.function.ToIntFunction<Minecraft> widthFn,
            java.util.function.ToIntFunction<Minecraft> heightFn
    ) {
        Entry entry = new Entry(label, getX, getY, setX, setY, widthFn, heightFn);
        ENTRIES.add(entry);
        return entry;
    }

    public record Entry(
            String label,
            IntSupplier getX,
            IntSupplier getY,
            IntConsumer setX,
            IntConsumer setY,
            java.util.function.ToIntFunction<Minecraft> widthFn,
            java.util.function.ToIntFunction<Minecraft> heightFn
    ) {
        public int resolvedX(GuiGraphics graphics, Minecraft mc, String sample) {
            int configured = getX.getAsInt();
            return configured >= 0 ? configured : HudLayout.resolveX(configured, graphics, mc, sample);
        }

        public int resolvedY(GuiGraphics graphics, Minecraft mc, int lineIndex) {
            int configured = getY.getAsInt();
            return configured >= 0 ? configured + lineIndex * (mc.font.lineHeight + 2)
                    : HudLayout.resolveY(configured, graphics, mc, lineIndex);
        }

        public int width(Minecraft mc) {
            return widthFn.applyAsInt(mc);
        }

        public int height(Minecraft mc) {
            return heightFn.applyAsInt(mc);
        }

        public boolean containsScreen(Minecraft mc, int mx, int my, String sample) {
            int x = screenX(mc, sample);
            int y = screenY(mc, 0);
            return mx >= x - 4 && mx < x + width(mc) + 4
                    && my >= y - 4 && my < y + height(mc) + 4;
        }

        public int screenX(Minecraft mc, String sample) {
            int configured = getX.getAsInt();
            if (configured >= 0) {
                return configured;
            }
            return mc.getWindow().getGuiScaledWidth() - mc.font.width(sample) - 4;
        }

        public int screenY(Minecraft mc, int lineIndex) {
            int configured = getY.getAsInt();
            if (configured >= 0) {
                return configured + lineIndex * (mc.font.lineHeight + 2);
            }
            int base = mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight - 4;
            return base - lineIndex * (mc.font.lineHeight + 2);
        }

        public boolean contains(GuiGraphics graphics, Minecraft mc, int mx, int my, String sample) {
            return containsScreen(mc, mx, my, sample);
        }

        public void moveTo(int x, int y) {
            setX.accept(snap(x));
            setY.accept(snap(y));
        }

        private static int snap(int value) {
            if (!HudLayout.snapToGrid) {
                return Math.max(0, value);
            }
            int g = HudLayout.gridSize;
            return Math.max(0, Math.round((float) value / g) * g);
        }
    }

    public static void resetAll() {
        HudLayout.coordsX = 4;
        HudLayout.coordsY = 4;
        HudLayout.clockX = 4;
        HudLayout.clockY = 18;
        HudLayout.blockCountX = 4;
        HudLayout.blockCountY = 32;
        HudLayout.fpsX = 4;
        HudLayout.fpsY = -1;
        HudLayout.pingX = 4;
        HudLayout.pingY = -1;
        HudLayout.moduleListX = -1;
        HudLayout.moduleListY = 4;
    }

    public static void drawLabel(GuiGraphics graphics, Minecraft mc, String text, int x, int y, int color) {
        Font font = mc.font;
        float scale = HudLayout.scale;
        int pad = 3;
        if (HudLayout.showBackground) {
            int w = (int) (font.width(text) * scale) + pad * 2;
            int h = (int) (font.lineHeight * scale) + pad;
            graphics.fill(x - pad, y - 2, x - pad + w, y - 2 + h, 0xA0101018);
        }
        if (Math.abs(scale - 1.0F) > 0.01F) {
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(scale, scale);
            graphics.drawString(font, text, 0, 0, color);
            pose.popMatrix();
        } else {
            graphics.drawString(font, text, x, y, color);
        }
    }
}
