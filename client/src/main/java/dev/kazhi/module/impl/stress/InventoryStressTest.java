package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressPayload;
import dev.kazhi.stressutil.StressSlotUtils;
import dev.kazhi.stressutil.StressPackets;
import dev.kazhi.stressutil.StressCreative;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.ThreadLocalRandom;

public class InventoryStressTest extends StressModule {
    public enum Mode {
        Pickup,
        ShiftClick,
        Swap,
        Mixed
    }

    public int packetsPerSecond = 80;
    public Mode mode = Mode.Mixed;
    public boolean fillInventory = false;
    public boolean heavyFill = false;
    public boolean rawPackets = false;

    private int slotCursor;
    private int actionCursor;
    private int pickupSlotA;
    private int pickupSlotB;
    private boolean pendingFill;

    public InventoryStressTest() {
        super("Inventory Stress", "Spams inventory click packets to stress-test server slot handling.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        slotCursor = StressSlotUtils.MAIN_START;
        actionCursor = 0;
        pickupSlotA = StressSlotUtils.MAIN_START;
        pickupSlotB = StressSlotUtils.MAIN_END;
        pendingFill = fillInventory && hasCreativeBuild();

        info("Inventory stress armed (~" + packetsPerSecond + " packets/s). Close the menu to start.");
    }

    @Override
    public void onTick() {
        if (!shouldRunStressTick()) {
            return;
        }

        if (pendingFill) {
            pendingFill = false;
            try {
                fillInventorySlots();
            } catch (Throwable t) {
                fail("Inventory fill failed.");
                dev.kazhi.rt.KazhiLog.error("Inventory stress fill failed", t);
                return;
            }
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            spamClick();
        }
    }

    private void spamClick() {
        int slotIndex = nextSlotIndex();
        int slotId = StressSlotUtils.menuSlotId(slotIndex);

        ClickType action = nextAction();
        int button = action == ClickType.SWAP ? player().getInventory().getSelectedSlot() : 0;

        if (rawPackets) {
            sendRawClick(slotId, button, action);
        } else {
            MC.gameMode.handleInventoryMouseClick(
                player().containerMenu.containerId,
                slotId,
                button,
                action,
                player()
            );
        }
    }

    private void sendRawClick(int slotId, int button, ClickType action) {
        AbstractContainerMenu handler = player().containerMenu;
        ItemStack carried = handler.getCarried().isEmpty() ? ItemStack.EMPTY : handler.getCarried().copy();
        connection().send(StressPackets.containerClick(handler, slotId, button, action, carried));
    }

    private ClickType nextAction() {
        return switch (mode) {
            case Pickup -> ClickType.PICKUP;
            case ShiftClick -> ClickType.QUICK_MOVE;
            case Swap -> ClickType.SWAP;
            case Mixed -> switch (actionCursor++ % 6) {
                case 0, 1 -> ClickType.PICKUP;
                case 2, 3 -> ClickType.QUICK_MOVE;
                default -> ClickType.SWAP;
            };
        };
    }

    private int nextSlotIndex() {
        if (mode == Mode.Pickup) {
            int slot = (actionCursor++ % 2 == 0) ? pickupSlotA : pickupSlotB;
            if (actionCursor % 40 == 0) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                pickupSlotA = random.nextInt(StressSlotUtils.MAIN_START, StressSlotUtils.MAIN_END + 1);
                pickupSlotB = random.nextInt(StressSlotUtils.MAIN_START, StressSlotUtils.MAIN_END + 1);
            }
            return slot;
        }

        if (mode == Mode.Mixed && actionCursor % 2 == 1) {
            return ThreadLocalRandom.current().nextInt(StressSlotUtils.MAIN_START, StressSlotUtils.MAIN_END + 1);
        }

        int index = slotCursor++;
        if (slotCursor > StressSlotUtils.MAIN_END) {
            slotCursor = StressSlotUtils.MAIN_START;
        }
        return index;
    }

    private void fillInventorySlots() {
        ItemStack stack = heavyFill
            ? StressPayload.copyStressShulker()
            : new ItemStack(Items.COBBLESTONE, 64);
        for (int i = StressSlotUtils.HOTBAR_START; i <= StressSlotUtils.MAIN_END; i++) {
            StressCreative.giveInventorySlot(player(), i, stack);
        }
    }
}
