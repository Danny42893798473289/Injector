package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;

public class JesusModule extends Module {
    public JesusModule() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        if (!player.isInWater() && !player.isInLava()) {
            return;
        }
        boolean fluid = mc.level.getFluidState(player.blockPosition()).is(FluidTags.WATER)
                || mc.level.getFluidState(player.blockPosition()).is(FluidTags.LAVA);
        if (!fluid && !player.isInWater() && !player.isInLava()) {
            return;
        }
        player.setDeltaMovement(player.getDeltaMovement().x, 0.11, player.getDeltaMovement().z);
        player.resetFallDistance();
        player.setOnGround(true);
    }
}
