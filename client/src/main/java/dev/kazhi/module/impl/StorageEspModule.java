package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;

public class StorageEspModule extends Module {
    private static StorageEspModule instance;
    public static int radius = 32;

    public StorageEspModule() {
        super("StorageESP", "Highlight chests and storage blocks nearby", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
