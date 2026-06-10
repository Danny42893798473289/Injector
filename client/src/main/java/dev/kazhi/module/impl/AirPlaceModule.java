package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Creative: place blocks in mid-air by finding a valid neighbor face. */
public class AirPlaceModule extends Module {
    public AirPlaceModule() {
        super("AirPlace", "Place blocks in air while holding use (creative)", Category.BUILD);
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
        if (!mc.options.keyUse.isDown()) {
            return;
        }
        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos clicked = blockHit.getBlockPos();
        BlockState clickedState = mc.level.getBlockState(clicked);
        BlockPos placePos = clickedState.isAir() || clickedState.canBeReplaced()
                ? clicked
                : clicked.relative(blockHit.getDirection());
        BlockState target = mc.level.getBlockState(placePos);
        if (!target.isAir() && !target.canBeReplaced()) {
            return;
        }

        BlockHitResult placeHit = hitForPlace(mc.level, placePos);
        if (placeHit != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, placeHit);
        }
    }

    private static BlockHitResult hitForPlace(net.minecraft.client.multiplayer.ClientLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isAir() || neighborState.canBeReplaced()) {
                continue;
            }
            return new BlockHitResult(Vec3.atCenterOf(pos), dir.getOpposite(), neighbor, false);
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isAir()) {
            return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, below, false);
        }
        return null;
    }
}
