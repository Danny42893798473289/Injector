package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressShulkerStacks;
import dev.kazhi.stressutil.StressSlotUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class ShulkerStressTest extends StressModule {
    public int perTick = 10;
    public int loreLines = StressShulkerStacks.DEFAULT_LORE_LINES;
    public int lineLength = StressShulkerStacks.DEFAULT_LINE_LENGTH;
    public boolean fillShulker = StressShulkerStacks.DEFAULT_FILL_SHULKER;

    private ItemStack heavyShulker;
    private int stackSignature;

    public ShulkerStressTest() {
        super("Shulker Stress", "Spawns heavy shulker boxes in creative and drops them rapidly.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireCreative() || !requireConnection()) {
            return;
        }
        invalidateStack();
        info("Stress test running. Watch TPS/MSPT on the server console.");
    }

    @Override
    protected void onDisable() {
        heavyShulker = null;
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }
        if (!hasCreativeBuild()) {
            fail("Creative mode required.");
            return;
        }

        ItemStack stack = getHeavyShulker().copy();
        int hotbarSlot = player().getInventory().getSelectedSlot();
        int packetSlot = StressSlotUtils.creativePacketSlot(hotbarSlot);

        for (int i = 0; i < perTick; i++) {
            player().getInventory().setItem(hotbarSlot, stack.copy());
            connection().send(new ServerboundSetCreativeModeSlotPacket(packetSlot, stack));
            connection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS,
                BlockPos.ZERO,
                Direction.DOWN
            ));
        }
    }

    public ItemStack copyHeavyShulker() {
        return getHeavyShulker().copy();
    }

    private ItemStack getHeavyShulker() {
        int signature = loreLines * 31 + lineLength * 17 + (fillShulker ? 1 : 0);
        if (heavyShulker == null || stackSignature != signature) {
            heavyShulker = StressShulkerStacks.build(loreLines, lineLength, fillShulker);
            stackSignature = signature;
        }
        return heavyShulker;
    }

    private void invalidateStack() {
        heavyShulker = null;
        stackSignature = 0;
    }
}
