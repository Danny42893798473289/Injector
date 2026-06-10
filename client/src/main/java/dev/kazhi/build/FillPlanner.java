package dev.kazhi.build;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class FillPlanner {
    private FillPlanner() {}

    public static List<BoxFillUndo.BlockChange> collectChanges(
            ClientLevel level,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState placeState,
            boolean hollowShell,
            boolean onlyAir,
            boolean allowFluidReplace,
            BlockState filterState
    ) {
        List<BoxFillUndo.BlockChange> changes = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (hollowShell) {
                        boolean onSurface = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                        if (!onSurface) {
                            continue;
                        }
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (filterState != null) {
                        if (existing.getBlock() != filterState.getBlock()) {
                            continue;
                        }
                    } else if (onlyAir && !existing.isAir()
                            && !(allowFluidReplace && existing.getFluidState().isSource())) {
                        continue;
                    }
                    if (existing.equals(placeState)) {
                        continue;
                    }
                    changes.add(new BoxFillUndo.BlockChange(pos, existing));
                }
            }
        }
        return changes;
    }
}
