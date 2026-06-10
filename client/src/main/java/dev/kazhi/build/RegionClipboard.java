package dev.kazhi.build;

import dev.kazhi.module.impl.BoxFillModule;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RegionClipboard {
    private static List<Entry> clipboard = List.of();
    private static int sizeX;
    private static int sizeY;
    private static int sizeZ;

    public static int pasteRotation;
    public static boolean mirrorX;
    public static boolean mirrorZ;

    private RegionClipboard() {}

    public record Entry(int dx, int dy, int dz, BlockState state) {}

    public static boolean hasClipboard() {
        return !clipboard.isEmpty();
    }

    public static void copyRegion() {
        BlockPos min = minCorner();
        BlockPos max = maxCorner();
        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
                    if (!state.isAir()) {
                        entries.add(new Entry(x - minX, y - minY, z - minZ, state));
                    }
                }
            }
        }
        clipboard = entries;
        sizeX = maxX - minX + 1;
        sizeY = maxY - minY + 1;
        sizeZ = maxZ - minZ + 1;
        KazhiLog.log("Clipboard: copied " + entries.size() + " blocks (" + sizeX + "x" + sizeY + "x" + sizeZ + ")");
    }

    public static int pasteAtCrosshair() {
        if (clipboard.isEmpty()) {
            KazhiLog.log("Clipboard: nothing to paste.");
            return 0;
        }
        Optional<BlockPos> origin = BoxFillModule.pickCrosshairBlock();
        if (origin.isEmpty()) {
            KazhiLog.log("Clipboard: look at a block for paste origin.");
            return 0;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.getAbilities().instabuild) {
            KazhiLog.log("Clipboard: creative mode required.");
            return 0;
        }
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null) {
            return 0;
        }
        ServerLevel level = server.getLevel(player.level().dimension());
        if (level == null) {
            return 0;
        }

        BlockPos base = origin.get();
        List<BoxFillUndo.BlockChange> changes = new ArrayList<>();
        int placed = 0;
        for (Entry entry : clipboard) {
            int[] t = transform(entry.dx(), entry.dy(), entry.dz(), sizeX, sizeY, sizeZ, pasteRotation, mirrorX, mirrorZ);
            BlockPos pos = base.offset(t[0], t[1], t[2]);
            BlockState existing = level.getBlockState(pos);
            if (level.setBlock(pos, entry.state(), 3)) {
                changes.add(new BoxFillUndo.BlockChange(pos, existing));
                placed++;
            }
        }
        BoxFillUndo.pushSnapshot(changes);
        KazhiLog.log("Clipboard: pasted " + placed + " blocks at " + base
                + " (rot=" + (pasteRotation * 90) + "°, mirrorX=" + mirrorX + ", mirrorZ=" + mirrorZ + ")");
        return placed;
    }

    static int[] transform(int dx, int dy, int dz, int sx, int sy, int sz, int rotation, boolean mx, boolean mz) {
        int x = mx ? sx - 1 - dx : dx;
        int z = mz ? sz - 1 - dz : dz;
        int curSx = sx;
        int curSz = sz;
        int steps = ((rotation % 4) + 4) % 4;
        for (int i = 0; i < steps; i++) {
            int nx = curSz - 1 - z;
            int nz = x;
            x = nx;
            z = nz;
            int tmp = curSx;
            curSx = curSz;
            curSz = tmp;
        }
        return new int[]{x, dy, z};
    }

    public static int countBlocksInSelection() {
        BlockPos min = minCorner();
        BlockPos max = maxCorner();
        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return 0;
        }
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!mc.level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static BlockPos minCorner() {
        return new BlockPos(BoxFillModule.corner1X, BoxFillModule.corner1Y, BoxFillModule.corner1Z);
    }

    private static BlockPos maxCorner() {
        return new BlockPos(BoxFillModule.corner2X, BoxFillModule.corner2Y, BoxFillModule.corner2Z);
    }
}
