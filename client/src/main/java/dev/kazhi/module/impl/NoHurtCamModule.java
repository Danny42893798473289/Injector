package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;

public class NoHurtCamModule extends Module {
    private static NoHurtCamModule instance;

    public NoHurtCamModule() {
        super("NoHurtCam", "Disable hurt camera shake", Category.MISC);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
