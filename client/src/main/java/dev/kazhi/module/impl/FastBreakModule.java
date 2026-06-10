package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

import java.lang.reflect.Field;

public class FastBreakModule extends Module {
    private static Field destroyDelayField;
    private static boolean fieldResolved;

    public FastBreakModule() {
        super("FastBreak", "Mine blocks faster client-side", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        if (!mc.options.keyAttack.isDown()) {
            return;
        }
        if (!resolveField()) {
            return;
        }
        try {
            destroyDelayField.setInt(mc.gameMode, 0);
        } catch (IllegalAccessException e) {
            KazhiLog.error("FastBreak: could not reset destroyDelay", e);
        }
    }

    private static boolean resolveField() {
        if (fieldResolved) {
            return destroyDelayField != null;
        }
        fieldResolved = true;
        try {
            destroyDelayField = MultiPlayerGameMode.class.getDeclaredField("destroyDelay");
            destroyDelayField.setAccessible(true);
            return true;
        } catch (Throwable t) {
            KazhiLog.error("FastBreak: destroyDelay field not found", t);
            return false;
        }
    }
}
