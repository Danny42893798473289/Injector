package dev.kazhi.module.impl.stress;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class PositionElytraStressTest extends StressModule {
    public int packetsPerSecond = 100;
    public int chunkStride = 32;
    public boolean elytraBoost = true;

    private double baseX;
    private double baseY;
    private double baseZ;
    private int step;

    public PositionElytraStressTest() {
        super("Position Stress", "Spams position packets with large jumps to stress chunk loading.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        baseX = player().getX();
        baseY = player().getY();
        baseZ = player().getZ();
        step = 0;

        info("Position stress running (~" + packetsPerSecond + " packets/s).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            if (elytraBoost && step % 4 == 0) {
                connection().send(new ServerboundPlayerCommandPacket(player(), ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }

            sendMovePacket();
            step++;
        }
    }

    private void sendMovePacket() {
        int stride = chunkStride;
        double x = baseX + (step % 16) * stride;
        double z = baseZ + ((step / 16) % 16) * stride;
        double y = baseY + 80;

        connection().send(new ServerboundMovePlayerPacket.PosRot(
            x,
            y,
            z,
            player().getYRot(),
            player().getXRot(),
            false,
            false
        ));
    }
}
