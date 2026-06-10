package dev.kazhi.build;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.impl.BoxFillModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;

public final class BoxFillPreview {
    private static int tickCounter;

    private BoxFillPreview() {}

    public static void tick() {
        if (!KazhiConfig.boxFillPreview) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (++tickCounter % 8 != 0) {
            return;
        }

        BlockPos min;
        BlockPos max;
        if (BoxFillModule.previewRegionMode) {
            min = new BlockPos(BoxFillModule.corner1X, BoxFillModule.corner1Y, BoxFillModule.corner1Z);
            max = new BlockPos(BoxFillModule.corner2X, BoxFillModule.corner2Y, BoxFillModule.corner2Z);
        } else {
            int r = Math.max(0, BoxFillModule.radius);
            BlockPos center = new BlockPos(BoxFillModule.centerX, BoxFillModule.centerY, BoxFillModule.centerZ);
            min = center.offset(-r, -r, -r);
            max = center.offset(r, r, r);
        }

        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());

        spawnCorner(mc, minX, minY, minZ);
        spawnCorner(mc, maxX, minY, minZ);
        spawnCorner(mc, minX, maxY, minZ);
        spawnCorner(mc, maxX, maxY, minZ);
        spawnCorner(mc, minX, minY, maxZ);
        spawnCorner(mc, maxX, minY, maxZ);
        spawnCorner(mc, minX, maxY, maxZ);
        spawnCorner(mc, maxX, maxY, maxZ);
    }

    private static void spawnCorner(Minecraft mc, int x, int y, int z) {
        mc.level.addParticle(ParticleTypes.END_ROD,
                x + 0.5, y + 0.5, z + 0.5, 0.0, 0.02, 0.0);
    }
}
