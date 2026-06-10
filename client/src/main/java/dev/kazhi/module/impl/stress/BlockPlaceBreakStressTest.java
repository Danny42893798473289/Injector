package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressSlotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlaceBreakStressTest extends StressModule {
    public int cyclesPerSecond = 20;
    public int placeSlot = 0;

    private BlockPos targetPos;

    public BlockPlaceBreakStressTest() {
        super("Block Stress", "Rapidly places and breaks blocks in front of you (creative).");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection() || !requireCreative()) {
            return;
        }

        targetPos = findPlacePos();
        if (targetPos == null) {
            fail("No valid placement position found.");
            return;
        }

        givePlaceBlock();
        info("Block stress at " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ());
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || targetPos == null) {
            return;
        }

        int cycles = Math.max(1, cyclesPerSecond / 20);
        if (cyclesPerSecond % 20 != 0) {
            cycles++;
        }

        for (int i = 0; i < cycles; i++) {
            placeBlock();
            breakBlock();
        }
    }

    private void placeBlock() {
        BlockPos support = targetPos.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false);

        StressSlotUtils.swapHotbar(placeSlot);
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void breakBlock() {
        Direction face = Direction.UP;
        connection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
            targetPos,
            face
        ));
        connection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
            targetPos,
            face
        ));
        MC.gameMode.startDestroyBlock(targetPos, face);
    }

    private void givePlaceBlock() {
        connection().send(new ServerboundSetCreativeModeSlotPacket(
            36 + placeSlot,
            new ItemStack(Items.STONE, 64)
        ));
    }

    private BlockPos findPlacePos() {
        BlockPos base = player().blockPosition().relative(player().getDirection(), 2);

        for (int dy = 0; dy <= 2; dy++) {
            BlockPos pos = base.above(dy);
            BlockPos below = pos.below();

            if (!MC.level.getBlockState(pos).canBeReplaced()) {
                continue;
            }
            if (!MC.level.getBlockState(below).isSolid()) {
                continue;
            }

            return pos;
        }

        return null;
    }
}
