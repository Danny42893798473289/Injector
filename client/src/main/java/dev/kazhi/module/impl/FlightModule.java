package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;

public class FlightModule extends Module {
    private static final float BUILD_SPEED = 0.10F;
    private float savedSpeed = 0.05F;

    public FlightModule() {
        super("Flight", "Creative-style flight for building (singleplayer)", Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        savedSpeed = abilities.getFlyingSpeed();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.setFlyingSpeed(BUILD_SPEED);
        player.onUpdateAbilities();
    }

    @Override
    protected void onDisable() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        abilities.flying = false;
        if (!abilities.instabuild) {
            abilities.mayfly = false;
        }
        abilities.setFlyingSpeed(savedSpeed);
        player.onUpdateAbilities();
    }

    @Override
    public void onTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        // Re-assert in case the integrated server resets abilities.
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            abilities.setFlyingSpeed(BUILD_SPEED);
            player.onUpdateAbilities();
        }
    }
}
