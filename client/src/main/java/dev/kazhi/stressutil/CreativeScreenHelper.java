package dev.kazhi.stressutil;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class CreativeScreenHelper {
    private static Method setSelectedTab;
    private static Field deleteItemSlotField;

    private CreativeScreenHelper() {}

    public static List<CreativeModeTab> stressTabs() {
        List<CreativeModeTab> tabs = new ArrayList<>();
        for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
            if (group != CreativeModeTabs.searchTab()) {
                tabs.add(group);
            }
        }
        return tabs;
    }

    public static void selectTab(CreativeModeInventoryScreen screen, CreativeModeTab group) {
        try {
            if (setSelectedTab == null) {
                setSelectedTab = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
                setSelectedTab.setAccessible(true);
            }
            setSelectedTab.invoke(screen, group);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static Slot destroySlot(CreativeModeInventoryScreen screen) {
        try {
            Field field = CreativeModeInventoryScreen.class.getDeclaredField("destroyItemSlot");
            field.setAccessible(true);
            return (Slot) field.get(screen);
        } catch (ReflectiveOperationException e) {
            try {
                if (deleteItemSlotField == null) {
                    deleteItemSlotField = CreativeModeInventoryScreen.class.getDeclaredField("deleteItemSlot");
                    deleteItemSlotField.setAccessible(true);
                }
                return (Slot) deleteItemSlotField.get(screen);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }
}
