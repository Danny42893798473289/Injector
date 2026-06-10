package dev.kazhi.rt;

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class StorageEspTypes {
    public record Color(float r, float g, float b, float a) {}

    private StorageEspTypes() {}

    public static boolean isStorage(BlockEntity be) {
        return be instanceof BaseContainerBlockEntity
                || be instanceof EnderChestBlockEntity
                || be instanceof HopperBlockEntity
                || be instanceof BrewingStandBlockEntity;
    }

    public static Color colorFor(BlockEntity be) {
        if (be instanceof EnderChestBlockEntity) {
            return new Color(0.55F, 0.2F, 0.9F, 0.95F);
        }
        if (be instanceof ShulkerBoxBlockEntity) {
            return new Color(0.75F, 0.35F, 0.95F, 0.95F);
        }
        if (be instanceof TrappedChestBlockEntity) {
            return new Color(1.0F, 0.45F, 0.35F, 0.95F);
        }
        if (be instanceof BarrelBlockEntity) {
            return new Color(0.85F, 0.55F, 0.25F, 0.95F);
        }
        if (be instanceof HopperBlockEntity) {
            return new Color(0.55F, 0.55F, 0.55F, 0.9F);
        }
        if (be instanceof AbstractFurnaceBlockEntity) {
            return new Color(0.7F, 0.45F, 0.25F, 0.9F);
        }
        if (be instanceof DispenserBlockEntity || be instanceof DropperBlockEntity) {
            return new Color(0.6F, 0.6F, 0.65F, 0.9F);
        }
        if (be instanceof BrewingStandBlockEntity) {
            return new Color(0.4F, 0.75F, 0.95F, 0.9F);
        }
        if (be instanceof ChestBlockEntity) {
            return new Color(1.0F, 0.85F, 0.2F, 0.95F);
        }
        return new Color(0.9F, 0.9F, 0.9F, 0.85F);
    }
}
