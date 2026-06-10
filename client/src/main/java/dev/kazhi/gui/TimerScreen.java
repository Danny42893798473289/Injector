package dev.kazhi.gui;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.impl.TimerModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimerScreen extends Screen {
    private final Screen parent;

    public TimerScreen(Screen parent) {
        super(Component.literal("Timer"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(
                Component.literal("Speed: " + String.format("%.2fx", TimerModule.speed)),
                b -> {
                    float next = TimerModule.speed + 0.25F;
                    if (next > 5.0F) {
                        next = 0.25F;
                    }
                    TimerModule.speed = next;
                    b.setMessage(Component.literal("Speed: " + String.format("%.2fx", next)));
                    KazhiConfig.save();
                }).bounds(cx - 110, 50, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 40, height - 32, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.drawCenteredString(font, "Timer (singleplayer integrated server)", width / 2, 20, 0xFFE8E8FF);
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
