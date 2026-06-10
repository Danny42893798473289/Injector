package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class NoSwingModule extends Module {
    public NoSwingModule() {
        super("NoSwing", "Hide hand swing animation", Category.MISC);
    }

    @Override
    public void onTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.swinging = false;
        player.swingTime = 0;
    }
}
