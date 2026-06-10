package dev.kazhi.stressutil;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public final class StressPackets {
    private StressPackets() {}

    public static ServerboundContainerClickPacket containerClick(
        AbstractContainerMenu handler,
        int slotId,
        int button,
        ClickType action,
        ItemStack carried
    ) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        HashedStack hashed = HashedStack.EMPTY;
        if (connection != null && !carried.isEmpty()) {
            try {
                hashed = HashedStack.create(carried, connection.decoratedHashOpsGenenerator());
            } catch (Throwable ignored) {
                hashed = HashedStack.EMPTY;
            }
        }

        return new ServerboundContainerClickPacket(
            handler.containerId,
            handler.getStateId(),
            (short) slotId,
            (byte) button,
            action,
            Int2ObjectMaps.emptyMap(),
            hashed
        );
    }
}
