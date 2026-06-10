package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.gui.ClientSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class FullbrightModule extends Module implements HasSettings {
    private static final double BRIGHT_GAMMA = 16.0;
    public static float strength = 1.0F;
    private double savedGamma = 1.0;
    private static FullbrightModule instance;

    public FullbrightModule() {
        super("Fullbright", "Adjustable brightness boost", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static float getStrength() {
        return isActive() ? strength : 0.0F;
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new ClientSettingsScreen(parent);
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            savedGamma = mc.options.gamma().get();
            applyGamma(mc);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.gamma().set(savedGamma);
        }
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            applyGamma(mc);
        }
    }

    private void applyGamma(Minecraft mc) {
        double target = 1.0 + (BRIGHT_GAMMA - 1.0) * strength;
        if (mc.options.gamma().get() < target) {
            mc.options.gamma().set(target);
        }
    }
}
