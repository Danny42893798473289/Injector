package dev.kazhi.module.impl.stress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.ItemStack;

public class SurvivalDropStressTest extends StressModule {
    public int packetsPerSecond = 40;
    public boolean dropAll = false;
    public boolean useDropPacket = true;

    public SurvivalDropStressTest() {
        super("Survival Drop", "Spams item drops without creative mode.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        if (player().isCreative()) {
            warn("You are in creative; this module is for survival drop stress.");
        }

        if (player().getMainHandItem().isEmpty()) {
            warn("Hold any item in your main hand for drops to affect the server.");
        }

        info("Survival drop stress running (~" + packetsPerSecond + "/s).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        ItemStack hand = player().getMainHandItem();
        if (hand.isEmpty()) {
            return;
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            if (useDropPacket) {
                connection().send(new ServerboundPlayerActionPacket(
                    dropAll ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS : ServerboundPlayerActionPacket.Action.DROP_ITEM,
                    BlockPos.ZERO,
                    Direction.DOWN
                ));
            }

            player().drop(dropAll);
        }
    }
}
