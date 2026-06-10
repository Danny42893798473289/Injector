package dev.kazhi.stressutil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.concurrent.ThreadLocalRandom;

public final class StressCrasherPayload {
    private static final String CHARS =
        "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";

    private StressCrasherPayload() {}

    public static String hugeString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static ItemStack heavyWrittenBook() {
        return StressWrittenBook.build(100, 1024, true);
    }

    public static ItemStack deepCustomDataItem() {
        CompoundTag root = new CompoundTag();
        CompoundTag current = new CompoundTag();

        for (int depth = 0; depth < 65; depth++) {
            CompoundTag next = new CompoundTag();
            next.put("r" + depth, current);
            next.putString("data", hugeString(1200));
            current = next;
        }

        root.put("tag", current);
        root.putString("author", hugeString(3000));

        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return stack;
    }

    public static ItemStack fireworkStack() {
        CompoundTag tag = new CompoundTag();
        CompoundTag firework = new CompoundTag();
        ListTag explosions = new ListTag();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 40; i++) {
            CompoundTag exp = new CompoundTag();
            exp.putIntArray("Colors", new int[]{random.nextInt(0xFFFFFF)});
            explosions.add(exp);
        }

        firework.put("Explosions", explosions);
        tag.put("Fireworks", firework);

        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static ItemStack armorStandStack() {
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", "minecraft:armor_stand");
        entityTag.putBoolean("Invisible", true);
        entityTag.putBoolean("NoGravity", true);
        entityTag.putString("CustomName", hugeString(512));

        CompoundTag tag = new CompoundTag();
        tag.put("EntityTag", entityTag);

        ItemStack stack = new ItemStack(Items.ARMOR_STAND);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}
