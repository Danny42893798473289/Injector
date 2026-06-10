package dev.kazhi.stressutil;

import dev.kazhi.module.ModuleManager;
import dev.kazhi.module.impl.stress.ShulkerStressTest;
import net.minecraft.world.item.ItemStack;

public final class StressPayload {
    private static ItemStack cachedDefault;

    private StressPayload() {}

    public static ItemStack copyStressShulker() {
        return ModuleManager.get()
            .get("Shulker Stress")
            .filter(m -> m instanceof ShulkerStressTest)
            .map(m -> ((ShulkerStressTest) m).copyHeavyShulker())
            .orElseGet(StressPayload::cachedDefaultShulker);
    }

    private static ItemStack cachedDefaultShulker() {
        if (cachedDefault == null) {
            cachedDefault = StressShulkerStacks.buildDefault();
        }
        return cachedDefault.copy();
    }
}
