package dev.kazhi.module.impl;

import dev.kazhi.build.BoxFillUndo;
import dev.kazhi.build.BuildAccess;
import dev.kazhi.build.PacketFill;
import dev.kazhi.gui.BoxFillScreen;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoxFillModule extends Module implements HasSettings {
    public static final int LIQUID_NONE = 0;
    public static final int LIQUID_WATER = 1;
    public static final int LIQUID_LAVA = 2;

    public static int centerX;
    public static int centerY = 64;
    public static int centerZ;
    public static int radius = 1;

    public static int corner1X;
    public static int corner1Y = 64;
    public static int corner1Z;
    public static int corner2X;
    public static int corner2Y = 64;
    public static int corner2Z;

    public static boolean hollow = false;
    public static boolean airOnly = true;
    public static boolean replaceMode;
    public static boolean previewRegionMode;
    public static boolean selectionOutline = true;
    public static boolean layerMode;
    public static int layerMinY = -1;
    public static int layerMaxY = -1;
    public static int liquidFillMode;

    private static final int MAX_RADIUS = 48;
    private static final int MAX_VOLUME = 125_000;
    private static BoxFillModule instance;

    public BoxFillModule() {
        super("BoxFill", "Creative box/region fill (RMB in GUI)", Category.BUILD);
        instance = this;
    }

    public static boolean isEnabledForPreview() {
        return instance != null && instance.isEnabled();
    }

    public static boolean hasValidRegion() {
        return corner1X != corner2X || corner1Y != corner2Y || corner1Z != corner2Z;
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new BoxFillScreen(parent);
    }

    public static Optional<BlockPos> pickCrosshairBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return Optional.empty();
        }
        HitResult hit = mc.player.pick(128.0, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        return Optional.of(((BlockHitResult) hit).getBlockPos());
    }

    public static void setPos1FromCrosshair() {
        pickCrosshairBlock().ifPresent(pos -> {
            corner1X = pos.getX();
            corner1Y = pos.getY();
            corner1Z = pos.getZ();
            previewRegionMode = true;
            KazhiLog.log("BoxFill pos1: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        });
    }

    public static void setPos2FromCrosshair() {
        pickCrosshairBlock().ifPresent(pos -> {
            corner2X = pos.getX();
            corner2Y = pos.getY();
            corner2Z = pos.getZ();
            previewRegionMode = true;
            KazhiLog.log("BoxFill pos2: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        });
    }

    public static void placeBox() {
        int r = Math.max(0, Math.min(radius, MAX_RADIUS));
        BlockPos center = new BlockPos(centerX, centerY, centerZ);
        BlockPos min = center.offset(-r, -r, -r);
        BlockPos max = center.offset(r, r, r);
        previewRegionMode = false;
        int placed = fillBetween(min, max, hollow, airOnly);
        KazhiLog.log("BoxFill: placed " + placed + " blocks (center " + centerX + ", " + centerY + ", "
                + centerZ + ", radius " + r + ", " + (hollow ? "hollow" : "solid") + ").");
    }

    public static void placeCustomRegion() {
        BlockPos a = new BlockPos(corner1X, corner1Y, corner1Z);
        BlockPos b = new BlockPos(corner2X, corner2Y, corner2Z);
        previewRegionMode = true;
        int placed = fillBetween(a, b, hollow, airOnly);
        KazhiLog.log("BoxFill: region placed " + placed + " blocks from " + a + " to " + b
                + " (" + (hollow ? "hollow" : "solid") + ").");
    }

    public static void clearRegion() {
        BlockPos a = new BlockPos(corner1X, corner1Y, corner1Z);
        BlockPos b = new BlockPos(corner2X, corner2Y, corner2Z);
        previewRegionMode = true;
        boolean savedReplace = replaceMode;
        boolean savedAir = airOnly;
        int savedLiquid = liquidFillMode;
        replaceMode = false;
        airOnly = false;
        liquidFillMode = LIQUID_NONE;
        int cleared = fillWithState(a, b, hollow, Blocks.AIR.defaultBlockState(), false);
        replaceMode = savedReplace;
        airOnly = savedAir;
        liquidFillMode = savedLiquid;
        KazhiLog.log("BoxFill: cleared " + cleared + " blocks.");
    }

    public static void undoLastFill() {
        BoxFillUndo.undoLast();
    }

    public static void copyRegion() {
        dev.kazhi.build.RegionClipboard.copyRegion();
    }

    public static void pasteRegion() {
        dev.kazhi.build.RegionClipboard.pasteAtCrosshair();
    }

    public static void exportSchematic(String name) {
        dev.kazhi.build.SchematicLoader.exportSelection(name);
    }

    public static void importSchematic(String name) {
        pickCrosshairBlock().ifPresent(pos -> dev.kazhi.build.SchematicLoader.loadAndPaste(name, pos));
    }

    public static int fillBetween(BlockPos from, BlockPos to, boolean hollowShell, boolean onlyAir) {
        return fillWithState(from, to, hollowShell, resolvePlaceState(), onlyAir);
    }

    private static int fillWithState(BlockPos from, BlockPos to, boolean hollowShell, BlockState placeState, boolean onlyAir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return 0;
        }
        if (!BuildAccess.hasCreativeBuild(player)) {
            KazhiLog.log("BoxFill: creative mode required.");
            return 0;
        }

        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());

        if (layerMode) {
            int sliceMin = layerMinY >= 0 ? layerMinY : minY;
            int sliceMax = layerMaxY >= 0 ? layerMaxY : maxY;
            minY = Math.max(minY, Math.min(sliceMin, sliceMax));
            maxY = Math.min(maxY, Math.max(sliceMin, sliceMax));
        }

        long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > MAX_VOLUME) {
            KazhiLog.log("BoxFill: region too large (" + volume + " blocks). Max " + MAX_VOLUME + ".");
            return 0;
        }

        BlockState filterState = null;
        if (replaceMode && player.getOffhandItem().getItem() instanceof BlockItem offItem) {
            filterState = offItem.getBlock().defaultBlockState();
        }

        ServerLevel level = BuildAccess.serverLevel(mc, player);
        if (level == null) {
            if (mc.level == null || mc.gameMode == null) {
                KazhiLog.log("BoxFill: not connected to a world.");
                return 0;
            }
            return PacketFill.startFill(
                    mc.level, player,
                    minX, minY, minZ, maxX, maxY, maxZ,
                    placeState, hollowShell, onlyAir, liquidFillMode != LIQUID_NONE, filterState);
        }

        List<BoxFillUndo.BlockChange> changes = new ArrayList<>();
        int placed = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (hollowShell) {
                        boolean onSurface = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                        if (!onSurface) {
                            continue;
                        }
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (replaceMode) {
                        if (filterState == null || existing.getBlock() != filterState.getBlock()) {
                            continue;
                        }
                    } else if (onlyAir && !existing.isAir()
                            && !(liquidFillMode != LIQUID_NONE && existing.getFluidState().isSource())) {
                        continue;
                    }
                    if (level.setBlock(pos, placeState, 3)) {
                        changes.add(new BoxFillUndo.BlockChange(pos, existing));
                        placed++;
                    }
                }
            }
        }
        BoxFillUndo.pushSnapshot(changes);
        return placed;
    }

    private static BlockState resolvePlaceState() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return Blocks.STONE.defaultBlockState();
        }
        if (liquidFillMode == LIQUID_WATER) {
            return Blocks.WATER.defaultBlockState();
        }
        if (liquidFillMode == LIQUID_LAVA) {
            return Blocks.LAVA.defaultBlockState();
        }
        if (player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.getFluidState().getType() == Fluids.WATER || state.getFluidState().getType() == Fluids.LAVA) {
                return state;
            }
            return state;
        }
        if (player.getOffhandItem().getItem() instanceof BlockItem offItem) {
            return offItem.getBlock().defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }
}
