package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;

public class NoFallModule extends Module {
    private static NoFallModule instance;

    public NoFallModule() {
        super("NoFall", "Prevent fall damage (client + packet)", Category.MOVEMENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public void onTick() {
        applyPre();
    }

    /** Called at end of LocalPlayer.tick — after movement, before packets go out. */
    public static void applyPost() {
        if (!isActive()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean falling = !player.onGround()
                && !player.getAbilities().flying
                && !player.isPassenger()
                && (player.getDeltaMovement().y < -0.01 || player.fallDistance > 1.5F);

        clearFall(player);

        if (falling) {
            player.setOnGround(true);
        }

        syncIntegratedServer(player, falling);
    }

    private static void applyPre() {
        if (!isActive()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        clearFall(player);
    }

    private static void clearFall(LocalPlayer player) {
        player.resetFallDistance();
        player.fallDistance = 0.0F;
    }

    private static void syncIntegratedServer(LocalPlayer player, boolean spoofGround) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return;
        }
        ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player.getUUID());
        if (serverPlayer == null) {
            return;
        }
        serverPlayer.resetFallDistance();
        serverPlayer.fallDistance = 0.0F;
        if (spoofGround) {
            serverPlayer.setOnGround(true);
        }
    }
}
