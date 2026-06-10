package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingHudModule extends Module {
    private static PingHudModule instance;

    public PingHudModule() {
        super("PingHUD", "Show server latency", Category.CLIENT);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static void render(GuiGraphics graphics, Minecraft mc) {
        if (!isActive() || mc.player == null) {
            return;
        }
        int ping = resolvePing(mc);
        String text = "Ping: " + ping + "ms";
        int color = ping < 80 ? 0xFF88FF88 : (ping < 150 ? 0xFFFFFF88 : 0xFFFF8888);
        int x = HudLayout.resolveX(HudLayout.pingX, graphics, mc, text);
        int y = HudLayout.resolveY(HudLayout.pingY, graphics, mc, 0);
        HudElements.drawLabel(graphics, mc, text, x, y, color);
    }

    private static int resolvePing(Minecraft mc) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return 0;
        }
        PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
        return info != null ? info.getLatency() : 0;
    }
}
