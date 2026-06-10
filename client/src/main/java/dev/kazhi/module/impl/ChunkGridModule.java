package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;

public class ChunkGridModule extends Module {
    private static ChunkGridModule instance;

    public ChunkGridModule() {
        super("ChunkGrid", "Draw chunk borders in the world", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
