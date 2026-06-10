package dev.kazhi.stressutil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class StressSlotUtils {
    public static final int HOTBAR_START = 0;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;

    private StressSlotUtils() {}

    public static boolean isHotbar(int index) {
        return index >= 0 && index <= 8;
    }

    public static boolean isArmor(int index) {
        return index >= ARMOR_START && index <= ARMOR_END;
    }

    public static int survivalSlotId(int index) {
        if (isHotbar(index)) {
            return 36 + index;
        }
        if (isArmor(index)) {
            return 5 + (index - ARMOR_START);
        }
        return index;
    }

    public static void swapHotbar(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.getInventory().setSelectedSlot(slot);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }
}
