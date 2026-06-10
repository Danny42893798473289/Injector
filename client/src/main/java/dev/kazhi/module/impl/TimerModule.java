package dev.kazhi.module.impl;

import dev.kazhi.gui.TimerScreen;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.ServerTickRateManager;

public class TimerModule extends Module implements HasSettings {
    public static float speed = 1.0F;
    private static final float DEFAULT_TICK_RATE = 20.0F;
    private static TimerModule instance;

    public TimerModule() {
        super("Timer", "Change game tick speed (singleplayer)", Category.MISC);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static float getSpeed() {
        return isActive() ? speed : 1.0F;
    }

    @Override
    protected void onEnable() {
        applyTickRate();
    }

    @Override
    protected void onDisable() {
        resetTickRate();
    }

    @Override
    public void onTick() {
        if (isEnabled()) {
            applyTickRate();
        }
    }

    private static void applyTickRate() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        ServerTickRateManager manager = server.tickRateManager();
        float target = DEFAULT_TICK_RATE * Math.max(0.25F, Math.min(speed, 5.0F));
        if (Math.abs(manager.tickrate() - target) > 0.01F) {
            manager.setTickRate(target);
            KazhiLog.log("Timer: tick rate " + target);
        }
    }

    private static void resetTickRate() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.tickRateManager().setTickRate(DEFAULT_TICK_RATE);
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new TimerScreen(parent);
    }
}
