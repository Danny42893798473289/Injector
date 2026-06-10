package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AnvilRenameStressTest extends StressModule {
    public int packetsPerSecond = 40;
    public int nameLength = 50;
    public boolean openAnvil = true;
    public boolean closeGui = false;

    private BlockPos anvilPos;
    private String renamePayload;

    public AnvilRenameStressTest() {
        super("Anvil Stress", "Spams anvil rename packets with long item names.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        anvilPos = resolveAnvilPos();
        renamePayload = StressText.line(nameLength);

        if (player().isCreative()) {
            connection().send(new ServerboundSetCreativeModeSlotPacket(
                36 + player().getInventory().getSelectedSlot(),
                new ItemStack(Items.IRON_PICKAXE)
            ));
        }

        if (openAnvil && anvilPos != null) {
            openAnvilGui();
        } else if (anvilPos == null) {
            warn("No anvil found nearby; spamming rename packets anyway.");
        }

        info("Anvil rename stress running.");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null) {
            return;
        }

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            connection().send(new ServerboundRenameItemPacket(renamePayload));
        }

        if (closeGui && player().containerMenu instanceof AnvilMenu) {
            connection().send(new ServerboundContainerClosePacket(player().containerMenu.containerId));
        }
    }

    private void openAnvilGui() {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(anvilPos), Direction.UP, anvilPos, false);
        MC.gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private BlockPos resolveAnvilPos() {
        if (MC.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (isAnvil(MC.level.getBlockState(pos).getBlock())) {
                return pos;
            }
        }

        BlockPos origin = player().blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (isAnvil(MC.level.getBlockState(pos).getBlock())) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isAnvil(Block block) {
        return block instanceof AnvilBlock;
    }
}
