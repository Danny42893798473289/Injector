package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Creative: auto-place blocks ahead at feet while walking. */
public class BridgeModule extends Module {
    public BridgeModule() {
        super("Bridge", "Place blocks ahead while moving (creative)", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        if (!BuildAccess.hasCreativeBuild(player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }
        if (!isMoving(player)) {
            return;
        }

        Direction forward = player.getDirection();
        BlockPos target = player.blockPosition().below().relative(forward);
        if (!mc.level.getBlockState(target).isAir()) {
            return;
        }

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
    }

    private static boolean isMoving(LocalPlayer player) {
        return player.input.getMoveVector().lengthSquared() > 0.01F;
    }
}
