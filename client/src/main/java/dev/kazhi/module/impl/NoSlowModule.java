package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NoSlowModule extends Module {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("kazhi", "noslow");
    private static final AttributeModifier BOOST = new AttributeModifier(
            MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);

    public NoSlowModule() {
        super("NoSlow", "Remove soul sand, honey, and item-use slowdown", Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        apply(true);
    }

    @Override
    protected void onDisable() {
        apply(false);
    }

    @Override
    public void onTick() {
        if (isEnabled()) {
            apply(true);
        }
    }

    private static void apply(boolean enabled) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        AttributeInstance efficiency = player.getAttribute(Attributes.MOVEMENT_EFFICIENCY);
        if (efficiency == null) {
            return;
        }
        if (enabled) {
            if (!efficiency.hasModifier(MODIFIER_ID)) {
                efficiency.addOrUpdateTransientModifier(BOOST);
            }
        } else {
            efficiency.removeModifier(MODIFIER_ID);
        }
    }
}
