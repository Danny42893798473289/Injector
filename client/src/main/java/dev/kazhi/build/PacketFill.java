package dev.kazhi.build;

import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Multiplayer fill via vanilla creative place/break packets — no OP or /fill needed.
 * One block in flight at a time, waits for server block updates to avoid ghost blocks.
 */
public final class PacketFill {
    private static final int CONFIRM_TIMEOUT = 40;
    private static final int MIN_CONFIRM_WAIT = 4;
    private static final int MAX_RETRIES = 2;
    private static final int COOLDOWN_TICKS = 2;
    private static final int STALL_WARN = 200;

    private enum Op { PLACE, BREAK }

    private record Queued(Op op, BlockPos pos, BlockState target, BlockState before, int retries) {
        Queued(Op op, BlockPos pos, BlockState target, BlockState before) {
            this(op, pos, target, before, 0);
        }
    }

    private record Pending(Queued job, BlockState expect) {}

    private static final Deque<Queued> queue = new ArrayDeque<>();
    private static BlockState activePlaceState;
    private static Pending pending;
    private static int confirmWait;
    private static int cooldown;
    private static int stallTicks;

    private PacketFill() {}

    public static boolean isBusy() {
        return pending != null || !queue.isEmpty();
    }

    public static int startFill(
            ClientLevel level,
            LocalPlayer player,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState placeState,
            boolean hollowShell,
            boolean onlyAir,
            boolean allowFluidReplace,
            BlockState filterState
    ) {
        if (isBusy()) {
            KazhiLog.log("BoxFill: packet queue busy — wait for current job to finish.");
            return 0;
        }

        List<BoxFillUndo.BlockChange> changes = FillPlanner.collectChanges(
                level, minX, minY, minZ, maxX, maxY, maxZ,
                placeState, hollowShell, onlyAir, allowFluidReplace, filterState);
        if (changes.isEmpty()) {
            KazhiLog.log("BoxFill: nothing to place in selection.");
            return 0;
        }

        BoxFillUndo.pushSnapshot(changes);
        activePlaceState = placeState;
        boolean clearing = placeState.isAir();
        if (!clearing) {
            syncCreativeSlot(player, placeState);
        }

        List<Queued> jobs = new ArrayList<>(changes.size());
        for (BoxFillUndo.BlockChange change : changes) {
            if (clearing) {
                jobs.add(new Queued(Op.BREAK, change.pos(), null, change.state()));
            } else {
                jobs.add(new Queued(Op.PLACE, change.pos(), placeState, change.state()));
            }
        }
        enqueueSorted(jobs, clearing);

        KazhiLog.log("BoxFill: packet " + (clearing ? "clear" : "fill") + " queued " + changes.size()
                + " blocks (paced, server-synced).");
        return changes.size();
    }

    public static void startRestore(List<BoxFillUndo.BlockChange> changes) {
        if (isBusy()) {
            KazhiLog.log("BoxFill undo: packet queue busy.");
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<Queued> jobs = new ArrayList<>(changes.size());
        boolean breaking = false;
        for (int i = changes.size() - 1; i >= 0; i--) {
            BoxFillUndo.BlockChange change = changes.get(i);
            BlockState current = level.getBlockState(change.pos());
            if (change.state().isAir()) {
                jobs.add(new Queued(Op.BREAK, change.pos(), null, current));
                breaking = true;
            } else {
                activePlaceState = change.state();
                jobs.add(new Queued(Op.PLACE, change.pos(), change.state(), current));
            }
        }
        enqueueSorted(jobs, breaking);
        KazhiLog.log("BoxFill undo: packet restore queued " + changes.size() + " blocks.");
    }

    public static void tick() {
        if (queue.isEmpty() && pending == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        MultiPlayerGameMode gameMode = mc.gameMode;
        ClientLevel level = mc.level;
        if (player == null || gameMode == null || level == null || mc.screen != null) {
            return;
        }
        if (!BuildAccess.hasCreativeBuild(player)) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (pending != null) {
            confirmWait++;
            BlockState current = level.getBlockState(pending.job().pos());
            if (confirmWait >= MIN_CONFIRM_WAIT
                    && matchesExpectation(pending.job(), pending.expect(), current)) {
                pending = null;
                confirmWait = 0;
                stallTicks = 0;
                cooldown = COOLDOWN_TICKS;
                return;
            }
            if (confirmWait < CONFIRM_TIMEOUT) {
                return;
            }

            revertClientPrediction(level, pending.job());
            Queued failed = pending.job();
            if (failed.retries() < MAX_RETRIES) {
                queue.addFirst(new Queued(
                        failed.op(), failed.pos(), failed.target(), failed.before(), failed.retries() + 1));
            } else {
                KazhiLog.log("BoxFill: gave up on " + pending.job().pos().toShortString()
                        + " (server did not confirm).");
            }
            pending = null;
            confirmWait = 0;
            cooldown = COOLDOWN_TICKS;
            return;
        }

        if (queue.isEmpty()) {
            return;
        }

        Queued job = queue.pollFirst();
        if (job == null) {
            return;
        }

        if (!inReach(player, job.pos())) {
            queue.addLast(job);
            stallTicks++;
            if (stallTicks >= STALL_WARN && stallTicks % STALL_WARN == 0) {
                KazhiLog.log("BoxFill: waiting — move closer (blocks out of reach).");
            }
            return;
        }

        if (job.op() == Op.PLACE) {
            BlockState placeState = job.target() != null ? job.target() : activePlaceState;
            if (placeState == null || placeState.isAir()) {
                return;
            }
            if (!canPlaceNow(level, job.pos())) {
                queue.addLast(job);
                return;
            }
            syncCreativeSlot(player, placeState);
            BlockHitResult hit = hitForPlace(level, job.pos());
            if (hit == null) {
                queue.addLast(job);
                return;
            }
            gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
            pending = new Pending(job, placeState);
        } else {
            BlockState existing = level.getBlockState(job.pos());
            if (existing.isAir()) {
                return;
            }
            gameMode.destroyBlock(job.pos());
            pending = new Pending(job, Blocks.AIR.defaultBlockState());
        }

        confirmWait = 0;
        stallTicks = 0;
    }

    private static void enqueueSorted(List<Queued> jobs, boolean breaking) {
        Comparator<Queued> order = breaking
                ? Comparator.comparingInt((Queued q) -> q.pos().getY()).reversed()
                    .thenComparingInt(q -> q.pos().getX())
                    .thenComparingInt(q -> q.pos().getZ())
                : Comparator.comparingInt((Queued q) -> q.pos().getY())
                    .thenComparingInt(q -> q.pos().getX())
                    .thenComparingInt(q -> q.pos().getZ());
        jobs.sort(order);
        queue.clear();
        queue.addAll(jobs);
    }

    private static boolean matchesExpectation(Queued job, BlockState expect, BlockState current) {
        if (job.op() == Op.BREAK) {
            return current.isAir() || current.canBeReplaced();
        }
        return current.getBlock() == expect.getBlock();
    }

    private static void revertClientPrediction(ClientLevel level, Queued job) {
        BlockState current = level.getBlockState(job.pos());
        if (job.op() == Op.PLACE) {
            if (job.target() != null && current.getBlock() == job.target().getBlock()) {
                level.setBlock(job.pos(), job.before(), Block.UPDATE_CLIENTS);
            }
        } else if (job.op() == Op.BREAK && current.isAir()) {
            level.setBlock(job.pos(), job.before(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean inReach(LocalPlayer player, BlockPos pos) {
        double reach = player.blockInteractionRange();
        return player.canInteractWithBlock(pos, reach);
    }

    private static boolean canPlaceNow(ClientLevel level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.canBeReplaced()) {
            return false;
        }
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.isAir() && !neighborState.canBeReplaced()) {
                return true;
            }
        }
        return false;
    }

    private static BlockHitResult hitForPlace(ClientLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isAir() || neighborState.canBeReplaced()) {
                continue;
            }
            Direction face = dir.getOpposite();
            return new BlockHitResult(Vec3.atCenterOf(pos), face, neighbor, false);
        }
        return null;
    }

    private static void syncCreativeSlot(LocalPlayer player, BlockState state) {
        if (state == null || state.isAir()) {
            return;
        }
        ItemStack stack = new ItemStack(state.getBlock());
        int slot = player.getInventory().getSelectedSlot();
        ItemStack current = player.getInventory().getItem(slot);
        if (ItemStack.isSameItemSameComponents(current, stack)) {
            return;
        }
        player.getInventory().setItem(slot, stack);
        if (player.connection != null) {
            player.connection.send(new ServerboundSetCreativeModeSlotPacket(slot, stack));
        }
    }
}
