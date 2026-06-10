package dev.kazhi.module.impl;

import dev.kazhi.gui.ClientSettingsScreen;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Opens global key and slider settings. */
public class ClientModule extends Module implements HasSettings {
    public ClientModule() {
        super("Settings", "Menu, zoom, panic, and BoxFill keys", Category.CLIENT);
    }

    @Override
    public Screen createSettingsScreen(Minecraft mc, Screen parent) {
        return new ClientSettingsScreen(parent);
    }
}
