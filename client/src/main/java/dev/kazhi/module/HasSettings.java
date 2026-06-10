package dev.kazhi.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Modules that expose a settings screen from the ClickGUI (right-click). */
public interface HasSettings {
    Screen createSettingsScreen(Minecraft mc, Screen parent);
}
