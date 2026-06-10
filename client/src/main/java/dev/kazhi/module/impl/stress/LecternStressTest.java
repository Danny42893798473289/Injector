package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressSlotUtils;
import dev.kazhi.stressutil.StressWrittenBook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LecternStressTest extends StressModule {
    public int cyclesPerSecond = 15;
    public boolean giveItems = true;
    public int lecternSlot = 0;
    public int bookSlot = 1;
    public boolean closeGui = true;
    public int pages = 100;
    public int charsPerPage = 512;
    public boolean jsonPages = true;

    private BlockPos lecternPos;
    private ItemStack cachedBook;
    private int bookSignature;

    public LecternStressTest() {
        super("Lectern Stress", "Rapidly places lecterns, puts heavy books on them, and breaks them.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        lecternPos = findLecternPos();
        if (lecternPos == null) {
            fail("No valid placement spot in front of you.");
            return;
        }

        invalidateBook();

        if (giveItems && player().isCreative()) {
            giveHotbarItems();
        }

        info("Lectern stress at " + lecternPos.getX() + ", " + lecternPos.getY() + ", " + lecternPos.getZ());
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || lecternPos == null) {
            return;
        }

        int cycles = Math.max(1, cyclesPerSecond / 20);
        if (cyclesPerSecond % 20 != 0) {
            cycles++;
        }

        for (int i = 0; i < cycles; i++) {
            runCycle();
        }
    }

    private void runCycle() {
        placeLectern();
        placeBook();
        if (closeGui) {
            closeScreen();
        }
        breakLectern();
    }

    private void placeLectern() {
        BlockPos support = lecternPos.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false);

        StressSlotUtils.swapHotbar(lecternSlot);
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void placeBook() {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(lecternPos), Direction.UP, lecternPos, false);

        StressSlotUtils.swapHotbar(bookSlot);
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void breakLectern() {
        Direction face = Direction.UP;
        connection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
            lecternPos,
            face
        ));
        connection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
            lecternPos,
            face
        ));
        MC.gameMode.startDestroyBlock(lecternPos, face);
    }

    private void closeScreen() {
        connection().send(new ServerboundContainerClosePacket(player().containerMenu.containerId));
    }

    private BlockPos findLecternPos() {
        BlockPos base = player().blockPosition().relative(player().getDirection(), 2);

        for (int dy = 0; dy <= 2; dy++) {
            BlockPos pos = base.above(dy);
            BlockPos below = pos.below();

            if (!MC.level.getBlockState(pos).canBeReplaced()) {
                continue;
            }
            if (!MC.level.getBlockState(below).isSolid()) {
                continue;
            }

            return pos;
        }

        return null;
    }

    private ItemStack getBook() {
        int signature = pages * 31 + charsPerPage * 17 + (jsonPages ? 1 : 0);
        if (cachedBook == null || bookSignature != signature) {
            cachedBook = StressWrittenBook.build(pages, charsPerPage, jsonPages);
            bookSignature = signature;
        }
        return cachedBook;
    }

    private void invalidateBook() {
        cachedBook = null;
        bookSignature = 0;
    }

    private void giveHotbarItems() {
        giveHotbarStack(lecternSlot, new ItemStack(Items.LECTERN, 64));
        giveHotbarStack(bookSlot, getBook().copy());
    }

    private void giveHotbarStack(int hotbarIndex, ItemStack stack) {
        connection().send(new ServerboundSetCreativeModeSlotPacket(36 + hotbarIndex, stack));
    }
}
