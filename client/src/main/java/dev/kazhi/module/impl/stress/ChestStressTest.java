package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressPayload;
import dev.kazhi.stressutil.StressPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChestStressTest extends StressModule {
    public enum ClickMode {
        Pickup,
        ShiftClick,
        Mixed
    }

    public int cyclesPerSecond = 20;
    public int clicksPerCycle = 50;
    public int extraClicksPerTick = 25;
    public ClickMode clickMode = ClickMode.Mixed;
    public boolean rawPackets = true;
    public boolean spamCloseOpen = true;

    private BlockPos chestPos;
    private int clickCursor;
    private int actionCursor;

    public ChestStressTest() {
        super("Chest Stress", "Rapidly opens/closes a chest and spams slot clicks.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        chestPos = resolveTargetChest();
        if (chestPos == null) {
            fail("Look at a chest, trapped chest, barrel, or shulker box.");
            return;
        }

        clickCursor = 0;
        actionCursor = 0;
        info("Chest stress on " + chestPos.getX() + ", " + chestPos.getY() + ", " + chestPos.getZ());
    }

    @Override
    protected void onDisable() {
        if (player() != null && isChestOpen()) {
            closeChest();
        }
        chestPos = null;
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || chestPos == null) {
            return;
        }

        if (isChestOpen()) {
            for (int i = 0; i < extraClicksPerTick; i++) {
                spamChestClick();
            }
        }

        int cycles = Math.max(1, cyclesPerSecond / 20);
        if (cyclesPerSecond % 20 != 0) {
            cycles++;
        }

        for (int i = 0; i < cycles; i++) {
            runCycle();
        }
    }

    private void runCycle() {
        if (spamCloseOpen || !isChestOpen()) {
            if (isChestOpen()) {
                closeChest();
            }
            openChest();
        } else if (!isChestOpen()) {
            openChest();
        }

        for (int i = 0; i < clicksPerCycle; i++) {
            spamChestClick();
        }

        if (spamCloseOpen) {
            closeChest();
        }
    }

    private void openChest() {
        BlockHitResult hit = new BlockHitResult(
            Vec3.atCenterOf(chestPos),
            Direction.UP,
            chestPos,
            false
        );
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void closeChest() {
        int syncId = player().containerMenu.containerId;
        if (rawPackets) {
            connection().send(new ServerboundContainerClosePacket(syncId));
        } else {
            player().closeContainer();
        }
    }

    private void spamChestClick() {
        if (!isChestOpen()) {
            return;
        }

        AbstractContainerMenu handler = player().containerMenu;
        if (!(handler instanceof ChestMenu container)) {
            return;
        }

        int chestSlots = container.getRowCount() * 9;
        if (chestSlots <= 0) {
            return;
        }

        int slot = clickCursor++ % chestSlots;
        ClickType action = nextClickAction();
        int button = 0;

        ItemStack carried = handler.getCarried().isEmpty()
            ? StressPayload.copyStressShulker()
            : handler.getCarried().copy();

        if (rawPackets) {
            connection().send(StressPackets.containerClick(handler, slot, button, action, carried));
        } else {
            MC.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, action, player());
        }
    }

    private ClickType nextClickAction() {
        return switch (clickMode) {
            case Pickup -> ClickType.PICKUP;
            case ShiftClick -> ClickType.QUICK_MOVE;
            case Mixed -> (actionCursor++ % 3 == 0) ? ClickType.QUICK_MOVE : ClickType.PICKUP;
        };
    }

    private boolean isChestOpen() {
        return player().containerMenu instanceof ChestMenu;
    }

    private BlockPos resolveTargetChest() {
        if (MC.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (isStorageBlock(MC.level.getBlockState(pos).getBlock())) {
                return pos;
            }
        }

        BlockPos playerPos = player().blockPosition();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (isStorageBlock(MC.level.getBlockState(pos).getBlock())) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isStorageBlock(Block block) {
        return block instanceof ChestBlock
            || block instanceof TrappedChestBlock
            || block instanceof BarrelBlock
            || block instanceof ShulkerBoxBlock;
    }
}
