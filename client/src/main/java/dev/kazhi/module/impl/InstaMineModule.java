package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.util.GameModeBreakHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class InstaMineModule extends Module {
    public InstaMineModule() {
        super("InstaMine", "Instantly mine blocks in survival", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (BuildAccess.hasCreativeBuild(mc.player)) {
            return;
        }
        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(mc.level, pos) < 0.0F) {
            return;
        }

        MultiPlayerGameMode gameMode = mc.gameMode;
        GameModeBreakHelper.resetDestroyDelay(gameMode);

        Direction face = blockHit.getDirection();
        if (!gameMode.isDestroying()) {
            gameMode.startDestroyBlock(pos, face);
        }
        GameModeBreakHelper.setDestroyProgress(gameMode, 1.0F);
        if (gameMode.continueDestroyBlock(pos, face)) {
            gameMode.destroyBlock(pos);
        }
    }
}
