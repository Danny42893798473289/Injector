package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.CreativeScreenHelper;
import dev.kazhi.stressutil.StressPayload;
import dev.kazhi.stressutil.StressSlotUtils;
import dev.kazhi.stressutil.StressPackets;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CreativeInventoryStressTest extends StressModule {
    public int packetsPerSecond = 400;
    public boolean openMenu = true;
    public boolean cycleTabs = true;
    public boolean hotbarSlots = true;
    public boolean middleClickClone = true;
    public boolean destroySlot = true;
    public boolean pickupSpam = true;

    private List<CreativeModeTab> tabs;
    private int tabCursor;
    private int actionCursor;
    private int hotbarCursor;

    public CreativeInventoryStressTest() {
        super("Creative Menu Stress", "Spams creative inventory tabs, slot clicks, clone, and destroy-slot packets.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireCreative() || !requireConnection()) {
            return;
        }

        tabs = CreativeScreenHelper.stressTabs();
        tabCursor = 0;
        actionCursor = 0;
        hotbarCursor = 0;

        if (openMenu) {
            ensureCreativeScreen();
        }

        info("Creative menu stress (~" + packetsPerSecond + " packets/s).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        if (openMenu) {
            ensureCreativeScreen();
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            if (cycleTabs && MC.screen instanceof CreativeModeInventoryScreen screen) {
                cycleTab(screen);
            }
            runAction();
        }
    }

    private void ensureCreativeScreen() {
        if (MC.screen instanceof CreativeModeInventoryScreen) {
            return;
        }
        MC.setScreen(new CreativeModeInventoryScreen(
            player(),
            MC.level.enabledFeatures(),
            player().hasPermissions(2)
        ));
    }

    private void cycleTab(CreativeModeInventoryScreen screen) {
        if (tabs == null || tabs.isEmpty()) {
            return;
        }
        if (actionCursor++ % 4 != 0) {
            return;
        }

        CreativeModeTab group = tabs.get(tabCursor++ % tabs.size());
        CreativeScreenHelper.selectTab(screen, group);
    }

    private void runAction() {
        int phase = actionCursor++ % 12;

        if (hotbarSlots && (phase == 0 || phase == 1)) {
            spamHotbar();
            return;
        }

        if (middleClickClone && (phase == 2 || phase == 3)) {
            spamClone();
            return;
        }

        if (destroySlot && (phase == 4 || phase == 5)) {
            spamDestroy();
            return;
        }

        if (pickupSpam) {
            spamPickup();
        }
    }

    private void spamHotbar() {
        int slot = hotbarCursor++ % 9;
        connection().send(new ServerboundSetCarriedItemPacket(slot));

        ItemStack heavy = StressPayload.copyStressShulker().copy();
        heavy.setCount(heavy.getMaxStackSize());
        connection().send(new ServerboundSetCreativeModeSlotPacket(36 + slot, heavy));
    }

    private void spamClone() {
        AbstractContainerMenu handler = player().containerMenu;
        if (!(handler instanceof CreativeModeInventoryScreen.ItemPickerMenu)) {
            return;
        }

        int hotbarIndex = ThreadLocalRandom.current().nextInt(9);
        ItemStack heavy = StressPayload.copyStressShulker().copy();
        heavy.setCount(heavy.getMaxStackSize());

        int slotId = 36 + hotbarIndex;
        connection().send(new ServerboundSetCreativeModeSlotPacket(slotId, heavy));
        sendClick(handler, slotId, 2, ClickType.CLONE, heavy);
    }

    private void spamDestroy() {
        if (!(MC.screen instanceof CreativeModeInventoryScreen screen)) {
            return;
        }

        AbstractContainerMenu handler = player().containerMenu;
        Slot trash = CreativeScreenHelper.destroySlot(screen);
        if (trash == null) {
            return;
        }

        ItemStack heavy = StressPayload.copyStressShulker().copy();
        heavy.setCount(heavy.getMaxStackSize());

        MC.gameMode.handleCreativeModeItemAdd(heavy, trash.index);
        sendClick(handler, trash.index, 1, ClickType.THROW, heavy);
    }

    private void spamPickup() {
        AbstractContainerMenu handler = player().containerMenu;
        if (!(handler instanceof CreativeModeInventoryScreen.ItemPickerMenu)) {
            return;
        }

        int index = ThreadLocalRandom.current().nextInt(StressSlotUtils.HOTBAR_START, StressSlotUtils.MAIN_END + 1);
        int slotId = StressSlotUtils.survivalSlotId(index);

        ItemStack carried = StressPayload.copyStressShulker();
        ClickType action = actionCursor % 3 == 0 ? ClickType.SWAP : ClickType.PICKUP;
        int button = action == ClickType.SWAP ? player().getInventory().getSelectedSlot() : 0;
        sendClick(handler, slotId, button, action, carried);
    }

    private void sendClick(AbstractContainerMenu handler, int slotId, int button, ClickType action, ItemStack carried) {
        connection().send(StressPackets.containerClick(handler, slotId, button, action, carried));
    }
}
