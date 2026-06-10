package dev.kazhi.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kazhi.module.impl.BoxFillModule;
import dev.kazhi.rt.KazhiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SchematicLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FORMAT_VERSION = 2;

    private SchematicLoader() {}

    public static Path schematicsDir() {
        Path game = Minecraft.getInstance().gameDirectory.toPath();
        return game.resolve("config").resolve("kazhi").resolve("schematics");
    }

    public static boolean loadAndPaste(String name, BlockPos origin) {
        Path file = schematicsDir().resolve(name.endsWith(".json") ? name : name + ".json");
        if (!Files.exists(file)) {
            KazhiLog.log("Schematic not found: " + file);
            return false;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            SchematicFile data = GSON.fromJson(reader, SchematicFile.class);
            if (data == null || data.blocks == null) {
                return false;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSingleplayerServer() == null) {
                return false;
            }
            var lookup = mc.getSingleplayerServer().registryAccess().lookupOrThrow(Registries.BLOCK);
            List<RegionClipboard.Entry> entries = new ArrayList<>();
            for (Map<String, Object> block : data.blocks) {
                int x = ((Number) block.get("x")).intValue();
                int y = ((Number) block.get("y")).intValue();
                int z = ((Number) block.get("z")).intValue();
                BlockState state = null;
                if (block.containsKey("state")) {
                    state = BlockStateFormats.parseCommandString(lookup, String.valueOf(block.get("state")));
                }
                if (state == null && block.containsKey("id")) {
                    String id = String.valueOf(block.get("id"));
                    state = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id))
                            .map(b -> b.defaultBlockState())
                            .orElse(null);
                }
                if (state != null) {
                    entries.add(new RegionClipboard.Entry(x, y, z, state));
                }
            }
            pasteEntries(origin, entries);
            KazhiLog.log("Schematic pasted: " + name + " (" + entries.size() + " blocks)");
            return true;
        } catch (IOException e) {
            KazhiLog.error("Schematic load failed: " + file, e);
            return false;
        }
    }

    public static void exportSelection(String name) {
        Path dir = schematicsDir();
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(name.endsWith(".json") ? name : name + ".json");
            SchematicFile data = new SchematicFile();
            data.version = FORMAT_VERSION;
            data.blocks = new ArrayList<>();
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            BlockPos a = new BlockPos(BoxFillModule.corner1X, BoxFillModule.corner1Y, BoxFillModule.corner1Z);
            BlockPos b = new BlockPos(BoxFillModule.corner2X, BoxFillModule.corner2Y, BoxFillModule.corner2Z);
            int minX = Math.min(a.getX(), b.getX());
            int minY = Math.min(a.getY(), b.getY());
            int minZ = Math.min(a.getZ(), b.getZ());
            int maxX = Math.max(a.getX(), b.getX());
            int maxY = Math.max(a.getY(), b.getY());
            int maxZ = Math.max(a.getZ(), b.getZ());
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
                        if (state.isAir()) {
                            continue;
                        }
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("x", x - minX);
                        entry.put("y", y - minY);
                        entry.put("z", z - minZ);
                        entry.put("state", BlockStateFormats.toCommandString(state));
                        data.blocks.add(entry);
                    }
                }
            }
            Files.writeString(file, GSON.toJson(data));
            KazhiLog.log("Schematic exported: " + file + " (" + data.blocks.size() + " blocks, full states)");
        } catch (IOException e) {
            KazhiLog.error("Schematic export failed", e);
        }
    }

    private static void pasteEntries(BlockPos origin, List<RegionClipboard.Entry> entries) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        var level = mc.getSingleplayerServer().getLevel(mc.player.level().dimension());
        if (level == null) {
            return;
        }
        List<BoxFillUndo.BlockChange> changes = new ArrayList<>();
        for (RegionClipboard.Entry entry : entries) {
            int[] t = RegionClipboard.transform(
                    entry.dx(), entry.dy(), entry.dz(),
                    inferSize(entries, 0), inferSize(entries, 1), inferSize(entries, 2),
                    RegionClipboard.pasteRotation, RegionClipboard.mirrorX, RegionClipboard.mirrorZ);
            BlockPos pos = origin.offset(t[0], t[1], t[2]);
            BlockState existing = level.getBlockState(pos);
            if (level.setBlock(pos, entry.state(), 3)) {
                changes.add(new BoxFillUndo.BlockChange(pos, existing));
            }
        }
        BoxFillUndo.pushSnapshot(changes);
    }

    private static int inferSize(List<RegionClipboard.Entry> entries, int axis) {
        int max = 0;
        for (RegionClipboard.Entry entry : entries) {
            int v = switch (axis) {
                case 0 -> entry.dx();
                case 1 -> entry.dy();
                default -> entry.dz();
            };
            max = Math.max(max, v);
        }
        return max + 1;
    }

    private static class SchematicFile {
        int version = FORMAT_VERSION;
        List<Map<String, Object>> blocks;
    }
}
