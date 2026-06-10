package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SafeWalkModule extends Module {
    public SafeWalkModule() {
        super("SafeWalk", "Stop at block edges while moving", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !player.onGround()) {
            return;
        }
        if (player.zza == 0.0F && player.xxa == 0.0F) {
            return;
        }
        if (player.isShiftKeyDown() || player.getAbilities().flying) {
            return;
        }

        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        if (dx == 0.0 && dz == 0.0) {
            return;
        }

        double len = Math.sqrt(dx * dx + dz * dz);
        double nx = dx / len * 0.35;
        double nz = dz / len * 0.35;
        BlockPos ahead = BlockPos.containing(player.getX() + nx, player.getY() - 0.5, player.getZ() + nz);
        BlockState below = mc.level.getBlockState(ahead);
        if (below.isAir() || below.canBeReplaced()) {
            player.zza = 0.0F;
            player.xxa = 0.0F;
        }
    }
}
