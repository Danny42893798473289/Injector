package dev.kazhi.stressutil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

/** Creative give/drop helpers that sync client state and use both gameMode + packets. */
public final class StressCreative {
    private StressCreative() {}

    public static void giveInventorySlot(LocalPlayer player, int inventoryIndex, ItemStack stack) {
        if (player == null || stack == null) {
            return;
        }

        ItemStack copy = stack.copy();
        player.getInventory().setItem(inventoryIndex, copy);

        int menuSlot = StressSlotUtils.menuSlotId(inventoryIndex);
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode != null) {
            gameMode.handleCreativeModeItemAdd(copy, menuSlot);
        }

        if (player.connection != null) {
            player.connection.send(new ServerboundSetCreativeModeSlotPacket(menuSlot, copy));
        }
    }

    public static void giveHotbar(LocalPlayer player, int hotbarIndex, ItemStack stack) {
        giveInventorySlot(player, hotbarIndex, stack);
    }

    public static void dropHotbarStack(LocalPlayer player, int hotbarIndex) {
        if (player == null || player.connection == null) {
            return;
        }

        player.getInventory().setSelectedSlot(hotbarIndex);
        player.connection.send(new ServerboundSetCarriedItemPacket(hotbarIndex));

        int menuSlot = StressSlotUtils.menuSlotId(hotbarIndex);
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null) {
            player.connection.send(StressPackets.containerClick(menu, menuSlot, 1, ClickType.THROW, ItemStack.EMPTY));
        }

        player.connection.send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS,
            BlockPos.ZERO,
            Direction.DOWN
        ));
    }
}
