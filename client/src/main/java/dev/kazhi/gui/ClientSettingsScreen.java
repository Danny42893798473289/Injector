package dev.kazhi.gui;

import dev.kazhi.gui.HudEditScreen;
import dev.kazhi.config.KazhiConfig;
import dev.kazhi.config.KazhiInput;
import dev.kazhi.config.KazhiKeys;
import dev.kazhi.module.impl.FullbrightModule;
import dev.kazhi.module.impl.ZoomModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClientSettingsScreen extends Screen {
    private final Screen parent;
    private String binding = "";
    private int rowY;

    public ClientSettingsScreen(Screen parent) {
        super(Component.literal("Kazhi Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int cx = width / 2;
        rowY = 36;

        bindRow(cx, "Menu key", "menu", KazhiInput.menuKey);
        bindRow(cx, "Zoom key (hold)", "zoom", KazhiInput.zoomKey);
        bindRow(cx, "Panic key (disable all)", "panic", KazhiInput.panicKey);
        bindRow(cx, "BoxFill pos1", "pos1", KazhiInput.pos1Key);
        bindRow(cx, "BoxFill pos2", "pos2", KazhiInput.pos2Key);

        addRenderableWidget(Button.builder(
                Component.literal("Zoom strength: " + String.format("%.0f%%", ZoomModule.zoomMultiplier * 100)),
                b -> {
                    float next = ZoomModule.zoomMultiplier + 0.05F;
                    if (next > 0.95F) {
                        next = 0.15F;
                    }
                    ZoomModule.zoomMultiplier = next;
                    b.setMessage(Component.literal("Zoom strength: " + String.format("%.0f%%", next * 100)));
                    KazhiConfig.save();
                }).bounds(cx - 110, rowY, 220, 20).build());
        rowY += 26;

        addRenderableWidget(Button.builder(
                Component.literal("Fullbright: " + String.format("%.0f%%", FullbrightModule.strength * 100)),
                b -> {
                    float next = FullbrightModule.strength + 0.1F;
                    if (next > 1.01F) {
                        next = 0.0F;
                    }
                    FullbrightModule.strength = next;
                    b.setMessage(Component.literal("Fullbright: " + String.format("%.0f%%", next * 100)));
                    KazhiConfig.save();
                }).bounds(cx - 110, rowY, 220, 20).build());
        rowY += 26;

        addRenderableWidget(Button.builder(
                Component.literal("Profile: " + KazhiConfig.activeProfile),
                b -> {
                    String next = switch (KazhiConfig.activeProfile) {
                        case "default" -> "build";
                        case "build" -> "survival";
                        default -> "default";
                    };
                    KazhiConfig.switchProfile(next);
                    b.setMessage(Component.literal("Profile: " + next));
                }).bounds(cx - 110, rowY, 220, 20).build());
        rowY += 26;

        addRenderableWidget(Button.builder(
                Component.literal(KazhiConfig.boxFillPreview ? "Box preview: ON" : "Box preview: OFF"),
                b -> {
                    KazhiConfig.boxFillPreview = !KazhiConfig.boxFillPreview;
                    b.setMessage(Component.literal(KazhiConfig.boxFillPreview ? "Box preview: ON" : "Box preview: OFF"));
                    KazhiConfig.save();
                }).bounds(cx - 110, rowY, 220, 20).build());
        rowY += 26;

        addRenderableWidget(Button.builder(Component.literal("Open HUD editor"), b ->
                minecraft.setScreen(new HudEditScreen(this)))
                .bounds(cx - 110, rowY, 220, 20).build());
        rowY += 26;

        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 40, height - 32, 80, 20).build());
    }

    private void bindRow(int cx, String label, String id, int key) {
        String keyName = binding.equals(id) ? "Press key..." : KazhiKeys.name(key);
        addRenderableWidget(Button.builder(Component.literal(label + ": " + keyName), b -> binding = id)
                .bounds(cx - 110, rowY, 220, 20).build());
        rowY += 24;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                binding = "";
            } else if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                applyBinding(0);
            } else {
                applyBinding(keyCode);
            }
            init();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void applyBinding(int keyCode) {
        switch (binding) {
            case "menu" -> KazhiInput.menuKey = keyCode;
            case "zoom" -> KazhiInput.zoomKey = keyCode;
            case "panic" -> KazhiInput.panicKey = keyCode;
            case "pos1" -> KazhiInput.pos1Key = keyCode;
            case "pos2" -> KazhiInput.pos2Key = keyCode;
            default -> { }
        }
        binding = "";
        KazhiConfig.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.drawCenteredString(font, "Client settings", width / 2, 12, 0xFFE8E8FF);
        graphics.drawCenteredString(font, "Keys apply in-game immediately", width / 2, 24, 0xFF888888);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        KazhiConfig.save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
