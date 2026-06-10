package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;

public class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("AutoSprint", "Sprint while moving forward", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }
        if (player.isShiftKeyDown() || player.isPassenger() || player.isUnderWater()) {
            return;
        }
        if (player.hasEffect(MobEffects.BLINDNESS)) {
            return;
        }
        if (player.zza > 0.0F && !player.horizontalCollision) {
            player.setSprinting(true);
        }
    }
}
