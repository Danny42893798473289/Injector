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

import java.util.ArrayList;
import java.util.List;

public class MultiContainerStressTest extends StressModule {
    public int cyclesPerSecond = 10;
    public int clicksPerOpen = 25;
    public int searchRadius = 8;
    public int maxContainers = 8;
    public boolean rawPackets = true;

    private List<BlockPos> containers = new ArrayList<>();
    private int containerIndex;
    private int clickCursor;

    public MultiContainerStressTest() {
        super("Multi Container", "Cycles open/click/close across multiple nearby containers.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        containers = findContainers();
        if (containers.isEmpty()) {
            fail("No chests, barrels, or shulker boxes found nearby.");
            return;
        }

        containerIndex = 0;
        clickCursor = 0;
        info("Multi-container stress on " + containers.size() + " targets.");
    }

    @Override
    protected void onDisable() {
        if (player() != null && player().containerMenu instanceof ChestMenu) {
            closeContainer();
        }
        containers.clear();
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || containers.isEmpty()) {
            return;
        }

        int cycles = Math.max(1, cyclesPerSecond / 20);
        if (cyclesPerSecond % 20 != 0) {
            cycles++;
        }

        for (int i = 0; i < cycles; i++) {
            runContainerCycle();
        }
    }

    private void runContainerCycle() {
        BlockPos pos = containers.get(containerIndex);
        containerIndex = (containerIndex + 1) % containers.size();

        if (isContainerOpen()) {
            closeContainer();
        }
        openContainer(pos);

        for (int i = 0; i < clicksPerOpen; i++) {
            spamClick();
        }

        closeContainer();
    }

    private void openContainer(BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void closeContainer() {
        int syncId = player().containerMenu.containerId;
        if (rawPackets) {
            connection().send(new ServerboundContainerClosePacket(syncId));
        } else {
            player().closeContainer();
        }
    }

    private void spamClick() {
        if (!isContainerOpen()) {
            return;
        }

        AbstractContainerMenu handler = player().containerMenu;
        if (!(handler instanceof ChestMenu container)) {
            return;
        }

        int slots = container.getRowCount() * 9;
        if (slots <= 0) {
            return;
        }

        int slot = clickCursor++ % slots;
        ItemStack carried = handler.getCarried().isEmpty()
            ? StressPayload.copyStressShulker()
            : handler.getCarried().copy();

        if (rawPackets) {
            connection().send(StressPackets.containerClick(handler, slot, 0, ClickType.PICKUP, carried));
        } else {
            MC.gameMode.handleInventoryMouseClick(handler.containerId, slot, 0, ClickType.PICKUP, player());
        }
    }

    private boolean isContainerOpen() {
        return player().containerMenu instanceof ChestMenu;
    }

    private List<BlockPos> findContainers() {
        List<BlockPos> found = new ArrayList<>();
        BlockPos origin = player().blockPosition();
        int radius = searchRadius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!isStorageBlock(MC.level.getBlockState(pos).getBlock())) {
                        continue;
                    }

                    if (!found.contains(pos)) {
                        found.add(pos);
                    }
                    if (found.size() >= maxContainers) {
                        return found;
                    }
                }
            }
        }

        return found;
    }

    private static boolean isStorageBlock(Block block) {
        return block instanceof ChestBlock
            || block instanceof TrappedChestBlock
            || block instanceof BarrelBlock
            || block instanceof ShulkerBoxBlock;
    }
}
