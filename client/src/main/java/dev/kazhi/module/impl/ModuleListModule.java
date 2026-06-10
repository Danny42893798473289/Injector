package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.module.ModuleManager;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;

public class ModuleListModule extends Module {
    private static ModuleListModule instance;

    public ModuleListModule() {
        super("ModuleList", "Show enabled modules on the HUD", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        List<Module> enabled = ModuleManager.get().getModules().stream()
                .filter(Module::isEnabled)
                .filter(m -> m != instance)
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        if (enabled.isEmpty()) {
            return;
        }

        int y = HudLayout.moduleListY >= 0 ? HudLayout.moduleListY : 4;
        for (Module module : enabled) {
            String text = module.getName();
            int color = colorFor(module.getCategory());
            int x = HudLayout.resolveX(HudLayout.moduleListX, graphics, mc, text);
            HudElements.drawLabel(graphics, mc, text, x, y, color);
            y += mc.font.lineHeight + 2;
        }
    }

    private static int colorFor(Category category) {
        return switch (category) {
            case MOVEMENT -> 0xFF66CCFF;
            case RENDER -> 0xFFCC88FF;
            case BUILD -> 0xFFFFCC66;
            case MISC -> 0xFF88FFAA;
            case STRESS -> 0xFFAA55FF;
            case CLIENT -> 0xFFE8E8E8;
        };
    }
}
