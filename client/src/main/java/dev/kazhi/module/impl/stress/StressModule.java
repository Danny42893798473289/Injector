package dev.kazhi.module.impl.stress;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

public abstract class StressModule extends Module {
    protected static final Minecraft MC = Minecraft.getInstance();

    protected StressModule(String name, String description) {
        super(name, description, Category.STRESS);
    }

    protected LocalPlayer player() {
        return MC.player;
    }

    protected ClientPacketListener connection() {
        return MC.getConnection();
    }

    protected boolean requireConnection() {
        if (connection() == null) {
            fail("Not connected to a world.");
            return false;
        }
        return true;
    }

    protected boolean requireCreative() {
        if (player() == null || !player().isCreative()) {
            fail("Creative mode required.");
            return false;
        }
        return true;
    }

    protected void info(String msg) {
        KazhiLog.log("[Stress] " + getName() + ": " + msg);
    }

    protected void warn(String msg) {
        KazhiLog.log("[Stress] " + getName() + " warning: " + msg);
    }

    protected void fail(String msg) {
        KazhiLog.log("[Stress] " + getName() + " error: " + msg);
        setEnabled(false);
    }

    protected int packetsPerTick(int packetsPerSecond) {
        int perTick = Math.max(1, packetsPerSecond / 20);
        if (packetsPerSecond % 20 != 0) {
            perTick++;
        }
        return perTick;
    }
}
