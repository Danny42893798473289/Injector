package dev.kazhi.module.impl;

import dev.kazhi.build.BuildAccess;
import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Creative: extended block reach for building. */
public class ReachModule extends Module {
    public static double reach = 6.0D;
    private double previousReach = -1;

    public ReachModule() {
        super("Reach", "Extended block reach (creative)", Category.BUILD);
    }

    @Override
    protected void onEnable() {
        applyReach(Minecraft.getInstance().player);
    }

    @Override
    protected void onDisable() {
        restoreReach(Minecraft.getInstance().player);
    }

    @Override
    public void onTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !BuildAccess.hasCreativeBuild(player)) {
            return;
        }
        AttributeInstance attr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attr != null && attr.getBaseValue() != reach) {
            applyReach(player);
        }
    }

    private void applyReach(LocalPlayer player) {
        if (player == null) {
            return;
        }
        AttributeInstance attr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attr == null) {
            return;
        }
        if (previousReach < 0) {
            previousReach = attr.getBaseValue();
        }
        attr.setBaseValue(reach);
    }

    private void restoreReach(LocalPlayer player) {
        if (player == null || previousReach < 0) {
            previousReach = -1;
            return;
        }
        AttributeInstance attr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (attr != null) {
            attr.setBaseValue(previousReach);
        }
        previousReach = -1;
    }
}
