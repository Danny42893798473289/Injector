package dev.kazhi.rt;

import dev.kazhi.module.impl.BlockCountHudModule;
import dev.kazhi.module.impl.ChunkBoundsModule;
import dev.kazhi.module.impl.ClockHudModule;
import dev.kazhi.module.impl.CoordsHudModule;
import dev.kazhi.module.impl.CrosshairModule;
import dev.kazhi.module.impl.FpsHudModule;
import dev.kazhi.module.impl.ModuleListModule;
import dev.kazhi.module.impl.PingHudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public final class HudRenderer {
    private HudRenderer() {}

    public static void render(GuiGraphics graphics, float partialTick) {
        try {
            renderSafe(graphics);
        } catch (Throwable t) {
            if (KazhiHooks.hudErrorCount++ < 3) {
                KazhiLog.error("HUD render error", t);
            }
        }
    }

    private static void renderSafe(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        List<String> lines = new ArrayList<>();
        if (CoordsHudModule.isActive()) {
            lines.addAll(CoordsHudModule.lines(mc));
        }
        if (ChunkBoundsModule.isActive()) {
            lines.addAll(ChunkBoundsModule.lines(mc));
        }

        if (!lines.isEmpty()) {
            int x = HudLayout.coordsX;
            int y = HudLayout.coordsY;
            for (String line : lines) {
                HudElements.drawLabel(graphics, mc, line, x, y, 0xE0FFFFFF);
                y += mc.font.lineHeight + 2;
            }
        }

        if (ClockHudModule.isActive()) {
            ClockHudModule.render(graphics, mc);
        }
        if (BlockCountHudModule.isActive()) {
            BlockCountHudModule.render(graphics, mc);
        }
        if (ModuleListModule.isActive()) {
            ModuleListModule.render(graphics, mc);
        }
        if (FpsHudModule.isActive()) {
            FpsHudModule.render(graphics, mc);
        }
        if (PingHudModule.isActive()) {
            PingHudModule.render(graphics, mc);
        }
        if (CrosshairModule.isActive()) {
            CrosshairModule.render(graphics, mc);
        }
    }

    /** Shared helpers for HUD modules. */
    public static String formatCoords(Minecraft mc) {
        BlockPos pos = mc.player.blockPosition();
        ResourceKey<Level> dim = mc.level.dimension();
        String dimName = dim.location().getPath();
        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        String biomeName = biomeHolder.unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("unknown");
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        return String.format("XYZ %d %d %d  |  %s  |  %s  |  %.0f/%.0f",
                pos.getX(), pos.getY(), pos.getZ(), dimName, biomeName, yaw, pitch);
    }

    public static String formatChunkInfo(Minecraft mc) {
        BlockPos pos = mc.player.blockPosition();
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        int localX = pos.getX() - SectionPos.sectionToBlockCoord(chunkX);
        int localZ = pos.getZ() - SectionPos.sectionToBlockCoord(chunkZ);
        int toEast = 15 - localX;
        int toWest = localX;
        int toSouth = 15 - localZ;
        int toNorth = localZ;
        return String.format("Chunk %d, %d  (in-chunk %d, %d)  |  borders N%d S%d W%d E%d",
                chunkX, chunkZ, localX, localZ, toNorth, toSouth, toWest, toEast);
    }
}
