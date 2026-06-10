package dev.kazhi.build;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;

public final class BuildAccess {
    private BuildAccess() {}

    public static ServerLevel serverLevel(Minecraft mc, LocalPlayer player) {
        IntegratedServer integrated = mc.getSingleplayerServer();
        if (integrated == null || player == null) {
            return null;
        }
        return integrated.getLevel(player.level().dimension());
    }

    public static ClientPacketListener connection(Minecraft mc) {
        return mc.getConnection();
    }

    public static boolean isRemoteMultiplayer(Minecraft mc) {
        return mc.player != null && mc.getSingleplayerServer() == null && mc.getConnection() != null;
    }

    /** Creative / instabuild — some servers (incl. NetEase) sync instabuild late. */
    public static boolean hasCreativeBuild(LocalPlayer player) {
        if (player == null) {
            return false;
        }
        if (player.getAbilities().instabuild) {
            return true;
        }
        return player.hasInfiniteMaterials();
    }
}
