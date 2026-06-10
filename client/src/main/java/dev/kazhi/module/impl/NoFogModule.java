package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;

import java.lang.reflect.Field;

public class NoFogModule extends Module {
    private static NoFogModule instance;
    private static Field fogEnabledField;
    private static boolean fogFieldResolved;

    public NoFogModule() {
        super("NoFog", "Disable distance fog rendering", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    protected void onEnable() {
        setFogEnabled(false);
    }

    @Override
    protected void onDisable() {
        setFogEnabled(true);
    }

    @Override
    public void onTick() {
        if (isEnabled()) {
            setFogEnabled(false);
        }
    }

    private static void setFogEnabled(boolean enabled) {
        if (!resolveFogField()) {
            return;
        }
        try {
            fogEnabledField.setBoolean(null, enabled);
        } catch (IllegalAccessException e) {
            KazhiLog.error("NoFog: could not set fogEnabled", e);
        }
    }

    private static boolean resolveFogField() {
        if (fogFieldResolved) {
            return fogEnabledField != null;
        }
        fogFieldResolved = true;
        try {
            Class<?> fogClass = Class.forName("net.minecraft.client.renderer.fog.FogRenderer");
            fogEnabledField = fogClass.getDeclaredField("fogEnabled");
            fogEnabledField.setAccessible(true);
            return true;
        } catch (Throwable t) {
            KazhiLog.error("NoFog: FogRenderer.fogEnabled not found", t);
            return false;
        }
    }
}
