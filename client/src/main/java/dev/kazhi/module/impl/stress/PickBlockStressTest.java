package dev.kazhi.module.impl.stress;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickBlockStressTest extends StressModule {
    public int picksPerSecond = 80;
    public int scanRadius = 6;
    public boolean includeData = true;
    public boolean useCrosshair = true;
    public boolean scanBlockEntities = true;
    public boolean pickEntities = false;

    private List<BlockPos> targets;
    private int targetCursor;

    public PickBlockStressTest() {
        super("Pick Block Stress", "Spams creative pick-block on block entities and your crosshair target.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireCreative() || !requireConnection()) {
            return;
        }

        refreshTargets();
        targetCursor = 0;
        info("Pick-block stress (~" + picksPerSecond + " picks/s, " + targets.size() + " targets).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || MC.gameMode == null) {
            return;
        }

        if (scanBlockEntities && player().tickCount % 40 == 0) {
            refreshTargets();
        }

        for (int i = 0; i < packetsPerTick(picksPerSecond); i++) {
            pickOnce();
        }
    }

    private void pickOnce() {
        boolean data = includeData;

        if (useCrosshair) {
            HitResult hit = MC.hitResult;
            if (hit instanceof BlockHitResult blockHit) {
                MC.gameMode.handlePickItemFromBlock(blockHit.getBlockPos(), data);
                return;
            }
            if (pickEntities && hit instanceof EntityHitResult entityHit) {
                MC.gameMode.handlePickItemFromEntity(entityHit.getEntity(), data);
                return;
            }
        }

        if (scanBlockEntities && !targets.isEmpty()) {
            BlockPos pos = targets.get(targetCursor++ % targets.size());
            MC.gameMode.handlePickItemFromBlock(pos, data);
            return;
        }

        if (pickEntities) {
            Entity entity = findNearbyEntity();
            if (entity != null) {
                MC.gameMode.handlePickItemFromEntity(entity, data);
            }
        }
    }

    private void refreshTargets() {
        targets = new ArrayList<>();
        if (player() == null || MC.level == null) {
            return;
        }

        BlockPos origin = player().blockPosition();
        int r = scanRadius;

        BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r)).forEach(pos -> {
            BlockState state = MC.level.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof ChestBlock
                || block instanceof SignBlock
                || block instanceof SkullBlock
                || block instanceof LecternBlock
                || block == Blocks.CREEPER_HEAD
                || block == Blocks.PLAYER_HEAD
                || block == Blocks.DRAGON_HEAD) {
                targets.add(pos.immutable());
                return;
            }

            BlockEntity be = MC.level.getBlockEntity(pos);
            if (be != null) {
                targets.add(pos.immutable());
            }
        });

        if (targets.isEmpty()) {
            BlockPos front = origin.relative(player().getDirection(), 2);
            targets.add(front);
        }
    }

    private Entity findNearbyEntity() {
        AABB box = player().getBoundingBox().inflate(scanRadius);
        List<Entity> entities = MC.level.getEntities(player(), box, e -> e != null && e.isAlive());
        if (entities.isEmpty()) {
            return null;
        }
        return entities.get(ThreadLocalRandom.current().nextInt(entities.size()));
    }
}
