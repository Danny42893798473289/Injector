package dev.kazhi.module.impl;

import dev.kazhi.build.RegionClipboard;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BlockCountHudModule extends Module {
    private static BlockCountHudModule instance;
    private static int cachedCount;
    private static int tickCounter;

    public BlockCountHudModule() {
        super("BlockCount", "Block count for BoxFill region selection", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void tickHud() {
        if (!isActive()) {
            return;
        }
        if (++tickCounter % 20 == 0) {
            cachedCount = RegionClipboard.countBlocksInSelection();
        }
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        String text = "Selection blocks: " + cachedCount;
        int x = HudLayout.resolveX(HudLayout.blockCountX, graphics, mc, text);
        int y = HudLayout.resolveY(HudLayout.blockCountY, graphics, mc, 0);
        HudElements.drawLabel(graphics, mc, text, x, y, 0xE0FFFFFF);
    }
}
