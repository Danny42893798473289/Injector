package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StepModule extends Module {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("kazhi", "step");
    private static final AttributeModifier STEP = new AttributeModifier(
            MODIFIER_ID, 0.4, AttributeModifier.Operation.ADD_VALUE);

    public StepModule() {
        super("Step", "Step up full blocks without jumping", Category.MOVEMENT);
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
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step == null) {
            return;
        }
        if (enabled) {
            if (!step.hasModifier(MODIFIER_ID)) {
                step.addOrUpdateTransientModifier(STEP);
            }
        } else {
            step.removeModifier(MODIFIER_ID);
        }
    }
}
