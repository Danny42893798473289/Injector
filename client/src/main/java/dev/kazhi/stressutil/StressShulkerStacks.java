package dev.kazhi.stressutil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class StressShulkerStacks {
    public static final int DEFAULT_LORE_LINES = 50;
    public static final int DEFAULT_LINE_LENGTH = 128;
    public static final boolean DEFAULT_FILL_SHULKER = true;

    private StressShulkerStacks() {}

    public static ItemStack build(int loreLines, int lineLength, boolean fillShulker) {
        List<ItemStack> contents = new ArrayList<>(27);
        ItemStack innerTemplate = buildInnerStack(loreLines, lineLength);

        for (int i = 0; i < 27; i++) {
            if (fillShulker || i == 0) {
                contents.add(innerTemplate.copy());
            } else {
                contents.add(ItemStack.EMPTY);
            }
        }

        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        shulker.set(DataComponents.CUSTOM_NAME, Component.literal("Stress Test"));
        return shulker;
    }

    public static ItemStack buildDefault() {
        return build(DEFAULT_LORE_LINES, DEFAULT_LINE_LENGTH, DEFAULT_FILL_SHULKER);
    }

    private static ItemStack buildInnerStack(int loreLines, int lineLength) {
        String line = "A".repeat(lineLength);
        List<Component> lore = new ArrayList<>(loreLines);

        for (int i = 0; i < loreLines; i++) {
            lore.add(Component.literal(line));
        }

        ItemStack stack = new ItemStack(Items.PAPER, 64);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Payload"));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }
}
