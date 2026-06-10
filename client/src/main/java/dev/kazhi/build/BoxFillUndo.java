package dev.kazhi.build;

import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class BoxFillUndo {
    private static final int MAX_SNAPSHOTS = 8;
    private static final Deque<Snapshot> undoStack = new ArrayDeque<>();

    private BoxFillUndo() {}

    public record BlockChange(BlockPos pos, BlockState state) {}

    public record Snapshot(List<BlockChange> changes) {}

    public static void clear() {
        undoStack.clear();
    }

    public static void pushSnapshot(List<BlockChange> changes) {
        if (changes.isEmpty()) {
            return;
        }
        undoStack.push(new Snapshot(new ArrayList<>(changes)));
        while (undoStack.size() > MAX_SNAPSHOTS) {
            undoStack.removeLast();
        }
    }

    public static boolean undoLast() {
        if (undoStack.isEmpty()) {
            KazhiLog.log("BoxFill undo: nothing to undo.");
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }

        Snapshot snapshot = undoStack.pop();
        ServerLevel level = BuildAccess.serverLevel(mc, player);
        if (level != null) {
            int restored = 0;
            for (BlockChange change : snapshot.changes()) {
                if (level.setBlock(change.pos(), change.state(), 3)) {
                    restored++;
                }
            }
            KazhiLog.log("BoxFill undo: restored " + restored + " blocks.");
            return true;
        }

        if (mc.level == null || mc.gameMode == null) {
            KazhiLog.log("BoxFill undo: not connected.");
            undoStack.push(snapshot);
            return false;
        }

        PacketFill.startRestore(snapshot.changes());
        return true;
    }

    public static boolean hasUndo() {
        return !undoStack.isEmpty();
    }
}
