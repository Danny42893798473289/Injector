package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressPayload;
import dev.kazhi.stressutil.StressSlotUtils;
import dev.kazhi.stressutil.StressPackets;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class InventoryStressTest extends StressModule {
    public enum Mode {
        Pickup,
        ShiftClick,
        Swap,
        Mixed
    }

    public int packetsPerSecond = 500;
    public Mode mode = Mode.Mixed;
    public boolean fillInventory = true;
    public boolean rawPackets = true;

    private int slotCursor;
    private int actionCursor;
    private int pickupSlotA;
    private int pickupSlotB;

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

        if (fillInventory && player().isCreative()) {
            fillInventorySlots();
        }

        info("Inventory stress running (~" + packetsPerSecond + " packets/s).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            spamClick();
        }
    }

    private void spamClick() {
        int slotIndex = nextSlotIndex();
        int slotId = StressSlotUtils.survivalSlotId(slotIndex);

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
        ItemStack carried = handler.getCarried().isEmpty()
            ? StressPayload.copyStressShulker()
            : handler.getCarried().copy();

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
        ItemStack stack = StressPayload.copyStressShulker();
        for (int i = StressSlotUtils.HOTBAR_START; i <= StressSlotUtils.MAIN_END; i++) {
            int slotId = StressSlotUtils.survivalSlotId(i);
            connection().send(new ServerboundSetCreativeModeSlotPacket(slotId, stack.copy()));
        }
    }
}
