package dev.kazhi.util;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

public final class GameModeBreakHelper {
    private static Field destroyDelayField;
    private static Field destroyProgressField;
    private static boolean fieldsResolved;

    private GameModeBreakHelper() {}

    public static void resetDestroyDelay(MultiPlayerGameMode gameMode) {
        if (!resolveFields() || destroyDelayField == null) {
            return;
        }
        try {
            destroyDelayField.setInt(gameMode, 0);
        } catch (IllegalAccessException e) {
            KazhiLog.error("GameModeBreakHelper: destroyDelay", e);
        }
    }

    public static void setDestroyProgress(MultiPlayerGameMode gameMode, float progress) {
        if (!resolveFields() || destroyProgressField == null) {
            return;
        }
        try {
            destroyProgressField.setFloat(gameMode, progress);
        } catch (IllegalAccessException e) {
            KazhiLog.error("GameModeBreakHelper: destroyProgress", e);
        }
    }

    public static boolean tryBreak(
            MultiPlayerGameMode gameMode,
            ClientLevel level,
            LocalPlayer player,
            BlockPos pos
    ) {
        if (gameMode == null || level == null || player == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        double reach = player.blockInteractionRange();
        if (!player.canInteractWithBlock(pos, reach)) {
            return false;
        }

        if (BuildAccess.hasCreativeBuild(player)) {
            return gameMode.destroyBlock(pos);
        }

        resetDestroyDelay(gameMode);
        gameMode.startDestroyBlock(pos, Direction.UP);
        setDestroyProgress(gameMode, 1.0F);
        gameMode.continueDestroyBlock(pos, Direction.UP);
        return gameMode.destroyBlock(pos);
    }

    private static boolean resolveFields() {
        if (fieldsResolved) {
            return destroyDelayField != null || destroyProgressField != null;
        }
        fieldsResolved = true;
        try {
            destroyDelayField = MultiPlayerGameMode.class.getDeclaredField("destroyDelay");
            destroyDelayField.setAccessible(true);
        } catch (Throwable t) {
            KazhiLog.error("GameModeBreakHelper: destroyDelay field missing", t);
        }
        try {
            destroyProgressField = MultiPlayerGameMode.class.getDeclaredField("destroyProgress");
            destroyProgressField.setAccessible(true);
        } catch (Throwable t) {
            KazhiLog.error("GameModeBreakHelper: destroyProgress field missing", t);
        }
        return destroyDelayField != null || destroyProgressField != null;
    }
}
