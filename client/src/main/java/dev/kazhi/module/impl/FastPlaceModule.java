package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class FastPlaceModule extends Module {
    private static Field rightClickDelayField;
    private static boolean fieldResolved;

    public FastPlaceModule() {
        super("FastPlace", "Remove right-click place delay", Category.BUILD);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (!resolveField()) {
            return;
        }
        try {
            rightClickDelayField.setInt(mc, 0);
        } catch (IllegalAccessException e) {
            KazhiLog.error("FastPlace: could not reset rightClickDelay", e);
        }
    }

    private static boolean resolveField() {
        if (fieldResolved) {
            return rightClickDelayField != null;
        }
        fieldResolved = true;
        for (String name : new String[]{"rightClickDelay", "rightClickDelayTimer"}) {
            try {
                rightClickDelayField = Minecraft.class.getDeclaredField(name);
                rightClickDelayField.setAccessible(true);
                return true;
            } catch (NoSuchFieldException ignored) {
            }
        }
        KazhiLog.log("FastPlace: rightClickDelay field not found");
        return false;
    }
}
