package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Creative: refill hotbar block stacks when they run low. */
public class ReplenishModule extends Module {
    public static int minStack = 16;

    public ReplenishModule() {
        super("Replenish", "Refill block stacks in hotbar (creative)", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }
        if (!BuildAccess.hasCreativeBuild(player)) {
            return;
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof BlockItem)) {
                continue;
            }
            if (stack.getCount() >= minStack) {
                continue;
            }
            ItemStack refill = stack.copy();
            refill.setCount(refill.getMaxStackSize());
            player.getInventory().setItem(slot, refill);
            if (player.connection != null) {
                player.connection.send(new ServerboundSetCreativeModeSlotPacket(slot, refill));
            }
        }
    }
}
