package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudRenderer;
import net.minecraft.client.Minecraft;

import java.util.List;

public class CoordsHudModule extends Module {
    private static CoordsHudModule instance;

    public CoordsHudModule() {
        super("CoordsHUD", "XYZ, dimension, and biome overlay", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static List<String> lines(Minecraft mc) {
        return List.of(HudRenderer.formatCoords(mc));
    }
}
