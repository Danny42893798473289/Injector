package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Creative: jump and place blocks under you to tower up. */
public class TowerModule extends Module {
    public TowerModule() {
        super("Tower", "Hold jump to pillar up with blocks (creative)", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        if (!BuildAccess.hasCreativeBuild(mc.player)) {
            return;
        }
        if (!mc.options.keyJump.isDown()) {
            return;
        }
        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).isAir()) {
            return;
        }

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }
}
