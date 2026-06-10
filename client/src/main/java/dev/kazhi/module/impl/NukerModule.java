package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.util.GameModeBreakHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NukerModule extends Module {
    /** Cube radius around the player (blocks). */
    public static int radius = 4;
    /** Max blocks broken per tick. */
    public static int blocksPerTick = 8;

    public NukerModule() {
        super("Nuker", "Break blocks around you while holding attack", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        List<BlockPos> targets = collectTargets(level, player);
        int broken = 0;
        for (BlockPos pos : targets) {
            if (broken >= blocksPerTick) {
                break;
            }
            if (GameModeBreakHelper.tryBreak(mc.gameMode, level, player, pos)) {
                broken++;
            }
        }
    }

    private static List<BlockPos> collectTargets(ClientLevel level, LocalPlayer player) {
        BlockPos center = player.blockPosition();
        int r = Math.max(1, Math.min(radius, 6));
        double reach = player.blockInteractionRange();
        double reachSq = reach * reach;
        Vec3 eyes = player.getEyePosition();

        List<BlockPos> targets = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (eyes.distanceToSqr(Vec3.atCenterOf(pos)) > reachSq) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }
                    if (!player.canInteractWithBlock(pos, reach)) {
                        continue;
                    }
                    targets.add(pos.immutable());
                }
            }
        }

        targets.sort(Comparator.comparingDouble(p -> eyes.distanceToSqr(Vec3.atCenterOf(p))));
        return targets;
    }
}
