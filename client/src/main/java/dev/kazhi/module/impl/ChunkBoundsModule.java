package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import dev.kazhi.rt.HudRenderer;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ChunkBoundsModule extends Module {
    private static ChunkBoundsModule instance;

    public ChunkBoundsModule() {
        super("ChunkBounds", "Chunk grid and distance to borders", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    public static List<String> lines(Minecraft mc) {
        return List.of(HudRenderer.formatChunkInfo(mc));
    }
}
