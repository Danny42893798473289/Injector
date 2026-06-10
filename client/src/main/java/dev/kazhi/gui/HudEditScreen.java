package dev.kazhi.gui;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.gui.GuiTheme;
import dev.kazhi.rt.HudElements;
import dev.kazhi.rt.HudLayout;
import dev.kazhi.rt.HudRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Drag-and-drop HUD layout editor with scale, grid, and background options. */
public class HudEditScreen extends Screen {
    private final Screen parent;
    private HudElements.Entry dragging;
    private int dragOffX;
    private int dragOffY;

    public HudEditScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(
                Component.literal(HudLayout.showBackground ? "Background: ON" : "Background: OFF"),
                b -> {
                    HudLayout.showBackground = !HudLayout.showBackground;
                    b.setMessage(Component.literal(HudLayout.showBackground ? "Background: ON" : "Background: OFF"));
                    KazhiConfig.save();
                }).bounds(cx - 154, height - 52, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal(HudLayout.snapToGrid ? "Grid snap: ON" : "Grid snap: OFF"),
                b -> {
                    HudLayout.snapToGrid = !HudLayout.snapToGrid;
                    b.setMessage(Component.literal(HudLayout.snapToGrid ? "Grid snap: ON" : "Grid snap: OFF"));
                    KazhiConfig.save();
                }).bounds(cx - 48, height - 52, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Scale: " + String.format("%.0f%%", HudLayout.scale * 100)),
                b -> {
                    float next = HudLayout.scale + 0.1F;
                    if (next > 1.51F) {
                        next = 0.7F;
                    }
                    HudLayout.scale = next;
                    b.setMessage(Component.literal("Scale: " + String.format("%.0f%%", next * 100)));
                    KazhiConfig.save();
                }).bounds(cx + 58, height - 52, 96, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Reset layout"), b -> {
            HudElements.resetAll();
            KazhiConfig.save();
        }).bounds(cx - 154, height - 28, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx + 58, height - 28, 96, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x40000000);
        HudRenderer.render(graphics, partialTick);

        GuiTheme theme = GuiTheme.current();
        for (HudElements.Entry entry : HudElements.all()) {
            String sample = sampleFor(entry);
            int x = entry.resolvedX(graphics, minecraft, sample);
            int y = entry.resolvedY(graphics, minecraft, 0);
            int w = entry.width(minecraft);
            int h = entry.height(minecraft);
            boolean hover = entry.contains(graphics, minecraft, mouseX, mouseY, sample);
            int border = dragging == entry ? theme.accent : (hover ? theme.enabled : theme.panelBorder);
            graphics.fill(x - 5, y - 5, x + w + 5, y + h + 5, 0x30000000);
            graphics.fill(x - 5, y - 5, x + w + 5, y - 4, border);
            graphics.fill(x - 5, y + h + 4, x + w + 5, y + h + 5, border);
            graphics.fill(x - 5, y - 5, x - 4, y + h + 5, border);
            graphics.fill(x + w + 4, y - 5, x + w + 5, y + h + 5, border);
            graphics.drawString(font, entry.label(), x, y - 12, theme.textDim);
        }

        graphics.fill(width / 2 - 160, 8, width / 2 + 160, 42, 0xE0101018);
        graphics.drawCenteredString(font, "HUD Editor — drag elements to reposition", width / 2, 14, theme.textTitle);
        graphics.drawCenteredString(font, "Enable HUD modules in ClickGUI to preview them live", width / 2, 26, theme.textDim);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = HudElements.all().size() - 1; i >= 0; i--) {
                HudElements.Entry entry = HudElements.all().get(i);
                String sample = sampleFor(entry);
                if (entry.containsScreen(minecraft, (int) mouseX, (int) mouseY, sample)) {
                    dragging = entry;
                    if (entry.getX().getAsInt() < 0) {
                        entry.setX().accept(entry.screenX(minecraft, sample));
                    }
                    if (entry.getY().getAsInt() < 0) {
                        entry.setY().accept(entry.screenY(minecraft, 0));
                    }
                    int x = entry.getX().getAsInt();
                    int y = entry.getY().getAsInt();
                    dragOffX = (int) mouseX - x;
                    dragOffY = (int) mouseY - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging != null) {
            dragging = null;
            KazhiConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging != null && button == 0) {
            dragging.moveTo((int) mouseX - dragOffX, (int) mouseY - dragOffY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private String sampleFor(HudElements.Entry entry) {
        if (entry == HudElements.FPS) {
            return "120 FPS";
        }
        if (entry == HudElements.PING) {
            return "Ping: 42ms";
        }
        if (entry == HudElements.CLOCK) {
            return "Time 12:00  |  TPS 20.0";
        }
        if (entry == HudElements.BLOCK_COUNT) {
            return "Selection blocks: 0";
        }
        if (entry == HudElements.MODULE_LIST) {
            return "ModuleList";
        }
        return "XYZ 0 64 0  |  overworld";
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
