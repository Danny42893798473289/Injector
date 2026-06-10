package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SpeedModule extends Module {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("kazhi", "speed");
    private static final AttributeModifier BOOST = new AttributeModifier(
            MODIFIER_ID, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public SpeedModule() {
        super("Speed", "25% movement speed boost", Category.MOVEMENT);
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
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (enabled) {
            if (!speed.hasModifier(MODIFIER_ID)) {
                speed.addOrUpdateTransientModifier(BOOST);
            }
        } else {
            speed.removeModifier(MODIFIER_ID);
        }
    }
}
