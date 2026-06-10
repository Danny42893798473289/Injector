package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.gui.ClientSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ZoomModule extends Module implements HasSettings {
    public static volatile boolean zoomKeyHeld;
    public static float zoomMultiplier = 0.35F;
    private static ZoomModule instance;

    public ZoomModule() {
        super("Zoom", "Hold zoom key to narrow FOV", Category.RENDER);
        instance = this;
    }

    public static float getFovMultiplier() {
        if (instance == null || !instance.isEnabled()) {
            return 1.0F;
        }
        return zoomKeyHeld ? zoomMultiplier : 1.0F;
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new ClientSettingsScreen(parent);
    }
}
