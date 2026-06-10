package dev.kazhi.build;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class BlockStateFormats {
    private BlockStateFormats() {}

    public static String toCommandString(BlockState state) {
        StringBuilder sb = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            Property<?> property = entry.getKey();
            Comparable<?> value = entry.getValue();
            parts.add(property.getName() + "=" + serialize(property, value));
        }
        parts.sort(Comparator.naturalOrder());
        if (!parts.isEmpty()) {
            sb.append('[').append(String.join(",", parts)).append(']');
        }
        return sb.toString();
    }

    public static BlockState parseCommandString(HolderLookup<Block> lookup, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return BlockStateParser.parseForBlock(lookup, new StringReader(raw.trim()), false).blockState();
        } catch (CommandSyntaxException e) {
            ResourceLocation id = ResourceLocation.tryParse(raw.trim());
            if (id == null) {
                return null;
            }
            return BuiltInRegistries.BLOCK.getOptional(id).map(Block::defaultBlockState).orElse(null);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Comparable<T>> String serialize(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
