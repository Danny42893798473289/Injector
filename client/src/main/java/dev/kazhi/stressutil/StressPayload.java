package dev.kazhi.stressutil;

import dev.kazhi.module.ModuleManager;
import dev.kazhi.module.impl.stress.ShulkerStressTest;
import net.minecraft.world.item.ItemStack;

public final class StressPayload {
    private StressPayload() {}

    public static ItemStack copyStressShulker() {
        return ModuleManager.get()
            .get("Shulker Stress")
            .filter(m -> m instanceof ShulkerStressTest)
            .map(m -> ((ShulkerStressTest) m).copyHeavyShulker())
            .orElseGet(StressShulkerStacks::buildDefault);
    }
}
