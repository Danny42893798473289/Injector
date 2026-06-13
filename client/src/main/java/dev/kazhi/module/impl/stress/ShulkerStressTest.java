package dev.kazhi.module.impl.stress;

import dev.kazhi.rt.KazhiLog;
import dev.kazhi.stressutil.StressCreative;
import dev.kazhi.stressutil.StressShulkerStacks;
import net.minecraft.world.item.ItemStack;

public class ShulkerStressTest extends StressModule {
    public int perTick = 5;
    public int loreLines = 16;
    public int lineLength = 64;
    public boolean fillShulker = false;

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
        try {
            getHeavyShulker();
        } catch (Throwable t) {
            fail("Failed to build stress shulker: " + t.getClass().getSimpleName());
            KazhiLog.error("Shulker stress payload build failed", t);
            return;
        }

        info("Stress test running. Close the menu and watch for dropped shulkers / server TPS.");
    }

    @Override
    protected void onDisable() {
        heavyShulker = null;
    }

    @Override
    public void onTick() {
        if (!shouldRunStressTick()) {
            return;
        }
        if (!hasCreativeBuild()) {
            fail("Creative mode required.");
            return;
        }

        ItemStack stack;
        try {
            stack = getHeavyShulker().copy();
        } catch (Throwable t) {
            fail("Failed to build stress shulker.");
            KazhiLog.error("Shulker stress payload build failed", t);
            return;
        }

        int hotbarSlot = player().getInventory().getSelectedSlot();
        for (int i = 0; i < perTick; i++) {
            StressCreative.giveHotbar(player(), hotbarSlot, stack);
            StressCreative.dropHotbarStack(player(), hotbarSlot);
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
