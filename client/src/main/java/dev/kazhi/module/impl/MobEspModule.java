package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;

public class MobEspModule extends Module {
    private static MobEspModule instance;
    public static int radius = 48;

    public MobEspModule() {
        super("MobESP", "Highlight nearby mobs and animals", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
