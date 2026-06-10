package dev.kazhi.module.impl;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.gui.XRayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XRayModule extends Module implements HasSettings {
    private static final Set<Block> EXTRA_BLOCKS = new HashSet<>();
    private static XRayModule instance;

    private static boolean coal = true;
    private static boolean iron = true;
    private static boolean copper = true;
    private static boolean gold = true;
    private static boolean redstone = true;
    private static boolean lapis = true;
    private static boolean diamond = true;
    private static boolean emerald = true;
    private static boolean ancientDebris = true;
    private static boolean netherGold = true;
    private static boolean netherQuartz = true;

    public XRayModule() {
        super("XRay", "See ores and selected blocks through walls", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new XRayScreen(parent);
    }

    @Override
    protected void onEnable() {
        reloadChunks();
    }

    @Override
    protected void onDisable() {
        reloadChunks();
    }

    private static void reloadChunks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    public static void applyOreFlags(KazhiConfig config) {
        if (config == null) {
            return;
        }
        coal = config.xrayCoal;
        iron = config.xrayIron;
        copper = config.xrayCopper;
        gold = config.xrayGold;
        redstone = config.xrayRedstone;
        lapis = config.xrayLapis;
        diamond = config.xrayDiamond;
        emerald = config.xrayEmerald;
        ancientDebris = config.xrayAncientDebris;
        netherGold = config.xrayNetherGold;
        netherQuartz = config.xrayNetherQuartz;
        applyExtraBlocks(config.xrayExtraBlocks);
    }

    public static void saveOreFlagsTo(KazhiConfig config) {
        config.xrayCoal = coal;
        config.xrayIron = iron;
        config.xrayCopper = copper;
        config.xrayGold = gold;
        config.xrayRedstone = redstone;
        config.xrayLapis = lapis;
        config.xrayDiamond = diamond;
        config.xrayEmerald = emerald;
        config.xrayAncientDebris = ancientDebris;
        config.xrayNetherGold = netherGold;
        config.xrayNetherQuartz = netherQuartz;
    }

    public static boolean isCoalEnabled() { return coal; }
    public static boolean isIronEnabled() { return iron; }
    public static boolean isCopperEnabled() { return copper; }
    public static boolean isGoldEnabled() { return gold; }
    public static boolean isRedstoneEnabled() { return redstone; }
    public static boolean isLapisEnabled() { return lapis; }
    public static boolean isDiamondEnabled() { return diamond; }
    public static boolean isEmeraldEnabled() { return emerald; }
    public static boolean isAncientDebrisEnabled() { return ancientDebris; }
    public static boolean isNetherGoldEnabled() { return netherGold; }
    public static boolean isNetherQuartzEnabled() { return netherQuartz; }

    public static void setCoalEnabled(boolean v) { coal = v; reloadChunks(); }
    public static void setIronEnabled(boolean v) { iron = v; reloadChunks(); }
    public static void setCopperEnabled(boolean v) { copper = v; reloadChunks(); }
    public static void setGoldEnabled(boolean v) { gold = v; reloadChunks(); }
    public static void setRedstoneEnabled(boolean v) { redstone = v; reloadChunks(); }
    public static void setLapisEnabled(boolean v) { lapis = v; reloadChunks(); }
    public static void setDiamondEnabled(boolean v) { diamond = v; reloadChunks(); }
    public static void setEmeraldEnabled(boolean v) { emerald = v; reloadChunks(); }
    public static void setAncientDebrisEnabled(boolean v) { ancientDebris = v; reloadChunks(); }
    public static void setNetherGoldEnabled(boolean v) { netherGold = v; reloadChunks(); }
    public static void setNetherQuartzEnabled(boolean v) { netherQuartz = v; reloadChunks(); }

    public static void applyExtraBlocks(List<String> ids) {
        EXTRA_BLOCKS.clear();
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null) {
                BuiltInRegistries.BLOCK.getOptional(loc).ifPresent(EXTRA_BLOCKS::add);
            }
        }
    }

    public static List<String> getExtraBlockIds() {
        return EXTRA_BLOCKS.stream()
                .map(b -> BuiltInRegistries.BLOCK.getKey(b).toString())
                .toList();
    }

    public static boolean shouldXRay(BlockState state) {
        if (!isActive()) {
            return false;
        }
        Block block = state.getBlock();
        if (EXTRA_BLOCKS.contains(block)) {
            return true;
        }
        if (coal && state.is(BlockTags.COAL_ORES)) {
            return true;
        }
        if (iron && state.is(BlockTags.IRON_ORES)) {
            return true;
        }
        if (copper && state.is(BlockTags.COPPER_ORES)) {
            return true;
        }
        if (gold && state.is(BlockTags.GOLD_ORES)) {
            return true;
        }
        if (redstone && state.is(BlockTags.REDSTONE_ORES)) {
            return true;
        }
        if (lapis && state.is(BlockTags.LAPIS_ORES)) {
            return true;
        }
        if (diamond && state.is(BlockTags.DIAMOND_ORES)) {
            return true;
        }
        if (emerald && state.is(BlockTags.EMERALD_ORES)) {
            return true;
        }
        if (ancientDebris && block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        if (netherGold && block == Blocks.NETHER_GOLD_ORE) {
            return true;
        }
        return netherQuartz && block == Blocks.NETHER_QUARTZ_ORE;
    }
}
