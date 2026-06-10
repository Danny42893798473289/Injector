package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;

public class NoJumpDelayModule extends Module {
    private static Field noJumpDelayField;
    private static boolean fieldResolved;

    public NoJumpDelayModule() {
        super("NoJumpDelay", "Remove jump cooldown between hops", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (!resolveField()) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        try {
            noJumpDelayField.setInt(player, 0);
        } catch (IllegalAccessException e) {
            KazhiLog.error("NoJumpDelay: could not reset noJumpDelay", e);
        }
    }

    private static boolean resolveField() {
        if (fieldResolved) {
            return noJumpDelayField != null;
        }
        fieldResolved = true;
        try {
            noJumpDelayField = LivingEntity.class.getDeclaredField("noJumpDelay");
            noJumpDelayField.setAccessible(true);
            return true;
        } catch (Throwable t) {
            KazhiLog.error("NoJumpDelay: noJumpDelay field not found", t);
            return false;
        }
    }
}
