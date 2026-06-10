package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressText;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.phys.BlockHitResult;

public class SignStressTest extends StressModule {
    public int packetsPerSecond = 40;
    public int lineLength = 384;
    public boolean bothSides = true;

    private BlockPos signPos;

    public SignStressTest() {
        super("Sign Stress", "Spams sign edit packets with large text.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        signPos = resolveSignPos();
        if (signPos == null) {
            fail("Look at a sign or stand near one.");
            return;
        }

        info("Sign stress on " + signPos.getX() + ", " + signPos.getY() + ", " + signPos.getZ());
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || signPos == null) {
            return;
        }

        String[] lines = StressText.signLines(lineLength);

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            sendEdit(signPos, true, lines);
            if (bothSides) {
                sendEdit(signPos, false, lines);
            }
        }
    }

    private void sendEdit(BlockPos pos, boolean front, String[] lines) {
        connection().send(new ServerboundSignUpdatePacket(pos, front, lines[0], lines[1], lines[2], lines[3]));
    }

    private BlockPos resolveSignPos() {
        if (MC.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (isSign(MC.level.getBlockState(pos).getBlock())) {
                return pos;
            }
        }

        BlockPos origin = player().blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (isSign(MC.level.getBlockState(pos).getBlock())) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSign(Block block) {
        return block instanceof SignBlock
            || block instanceof StandingSignBlock
            || block instanceof WallSignBlock;
    }
}
