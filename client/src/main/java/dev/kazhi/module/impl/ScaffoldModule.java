package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ScaffoldModule extends Module {
    public ScaffoldModule() {
        super("Scaffold", "Place a block under you at edges while walking", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }
        if (player.onGround() || player.getAbilities().flying) {
            return;
        }
        BlockPos below = player.blockPosition().below();
        if (!mc.level.getBlockState(below).isAir()) {
            return;
        }
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(below), Direction.UP, below, false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
    }
}
