package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class NoWeatherModule extends Module {
    public NoWeatherModule() {
        super("NoWeather", "Clear rain and thunder client-side", Category.RENDER);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        level.setRainLevel(0.0F);
        level.setThunderLevel(0.0F);
    }
}
