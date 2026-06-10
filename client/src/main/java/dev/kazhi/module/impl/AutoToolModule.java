package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoToolModule extends Module {
    public AutoToolModule() {
        super("AutoTool", "Switch to the best hotbar tool for the targeted block", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!mc.options.keyAttack.isDown()) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (state.isAir()) {
            return;
        }

        int bestSlot = -1;
        float bestSpeed = 1.0F;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }
        if (bestSlot >= 0 && bestSlot != player.getInventory().getSelectedSlot()) {
            player.getInventory().setSelectedSlot(bestSlot);
        }
    }
}
