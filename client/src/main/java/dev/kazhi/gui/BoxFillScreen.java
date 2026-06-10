package dev.kazhi.gui;

import dev.kazhi.build.RegionClipboard;
import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.impl.BoxFillModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class BoxFillScreen extends Screen {
    private enum Tab { BOX, REGION }

    private final Screen parent;
    private Tab tab = Tab.BOX;

    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private EditBox radiusField;

    private EditBox x1Field;
    private EditBox y1Field;
    private EditBox z1Field;
    private EditBox x2Field;
    private EditBox y2Field;
    private EditBox z2Field;
    private EditBox layerMinField;
    private EditBox layerMaxField;

    private Button hollowButton;
    private Button airOnlyButton;
    private Button replaceButton;
    private Button liquidButton;
    private Button layerButton;
    private Button outlineButton;

    public BoxFillScreen(Screen parent) {
        super(Component.literal("World Fill"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int cx = width / 2;

        addRenderableWidget(Button.builder(Component.literal(tab == Tab.BOX ? "> Box <" : "Box"),
                b -> switchTab(Tab.BOX)).bounds(cx - 155, 24, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal(tab == Tab.REGION ? "> Region <" : "Region"),
                b -> switchTab(Tab.REGION)).bounds(cx - 75, 24, 80, 20).build());

        hollowButton = Button.builder(Component.literal(hollowLabel()), b -> {
            BoxFillModule.hollow = !BoxFillModule.hollow;
            b.setMessage(Component.literal(hollowLabel()));
            KazhiConfig.save();
        }).bounds(cx + 15, 24, 140, 20).build();
        addRenderableWidget(hollowButton);

        airOnlyButton = Button.builder(Component.literal(airOnlyLabel()), b -> {
            BoxFillModule.airOnly = !BoxFillModule.airOnly;
            if (BoxFillModule.airOnly) {
                BoxFillModule.replaceMode = false;
            }
            b.setMessage(Component.literal(airOnlyLabel()));
            KazhiConfig.save();
        }).bounds(cx + 165, 24, 120, 20).build();
        addRenderableWidget(airOnlyButton);

        replaceButton = Button.builder(Component.literal(replaceLabel()), b -> {
            BoxFillModule.replaceMode = !BoxFillModule.replaceMode;
            if (BoxFillModule.replaceMode) {
                BoxFillModule.airOnly = false;
                BoxFillModule.liquidFillMode = BoxFillModule.LIQUID_NONE;
            }
            b.setMessage(Component.literal(replaceLabel()));
            KazhiConfig.save();
        }).bounds(cx - 155, 48, 150, 20).build();
        addRenderableWidget(replaceButton);

        liquidButton = Button.builder(Component.literal(liquidLabel()), b -> {
            BoxFillModule.liquidFillMode = (BoxFillModule.liquidFillMode + 1) % 3;
            if (BoxFillModule.liquidFillMode != BoxFillModule.LIQUID_NONE) {
                BoxFillModule.replaceMode = false;
            }
            b.setMessage(Component.literal(liquidLabel()));
            KazhiConfig.save();
        }).bounds(cx + 5, 48, 130, 20).build();
        addRenderableWidget(liquidButton);

        outlineButton = Button.builder(Component.literal(outlineLabel()), b -> {
            BoxFillModule.selectionOutline = !BoxFillModule.selectionOutline;
            b.setMessage(Component.literal(outlineLabel()));
            KazhiConfig.save();
        }).bounds(cx + 145, 48, 140, 20).build();
        addRenderableWidget(outlineButton);

        if (tab == Tab.BOX) {
            initBoxTab(cx);
        } else {
            initRegionTab(cx);
        }

        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 40, height - 28, 80, 20).build());
    }

    private void switchTab(Tab next) {
        applyFields();
        tab = next;
        BoxFillModule.previewRegionMode = tab == Tab.REGION;
        init();
    }

    private void initBoxTab(int cx) {
        int y = 78;
        xField = field(cx, y, BoxFillModule.centerX);
        yField = field(cx, y + 22, BoxFillModule.centerY);
        zField = field(cx, y + 44, BoxFillModule.centerZ);
        radiusField = field(cx, y + 66, BoxFillModule.radius);

        layerButton = Button.builder(Component.literal(layerLabel()), b -> {
            BoxFillModule.layerMode = !BoxFillModule.layerMode;
            b.setMessage(Component.literal(layerLabel()));
            KazhiConfig.save();
        }).bounds(cx - 110, y + 88, 220, 20).build();
        addRenderableWidget(layerButton);

        layerMinField = field(cx - 55, y + 112, BoxFillModule.layerMinY);
        layerMaxField = field(cx + 55, y + 112, BoxFillModule.layerMaxY);

        addRenderableWidget(Button.builder(Component.literal("Crosshair -> center"), b -> {
            BoxFillModule.pickCrosshairBlock().ifPresent(pos -> {
                xField.setValue(String.valueOf(pos.getX()));
                yField.setValue(String.valueOf(pos.getY()));
                zField.setValue(String.valueOf(pos.getZ()));
            });
        }).bounds(cx - 110, y + 136, 220, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Place box"), b -> {
            applyFields();
            BoxFillModule.placeBox();
        }).bounds(cx - 110, y + 162, 220, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Undo (U)"), b -> BoxFillModule.undoLastFill())
                .bounds(cx - 110, y + 188, 220, 20).build());
    }

    private void initRegionTab(int cx) {
        int y = 74;
        x1Field = field(cx - 55, y, BoxFillModule.corner1X);
        y1Field = field(cx - 55, y + 22, BoxFillModule.corner1Y);
        z1Field = field(cx - 55, y + 44, BoxFillModule.corner1Z);

        x2Field = field(cx + 55, y, BoxFillModule.corner2X);
        y2Field = field(cx + 55, y + 22, BoxFillModule.corner2Y);
        z2Field = field(cx + 55, y + 44, BoxFillModule.corner2Z);

        layerButton = Button.builder(Component.literal(layerLabel()), b -> {
            BoxFillModule.layerMode = !BoxFillModule.layerMode;
            b.setMessage(Component.literal(layerLabel()));
            KazhiConfig.save();
        }).bounds(cx - 110, y + 68, 105, 20).build();
        addRenderableWidget(layerButton);

        layerMinField = field(cx - 55, y + 92, BoxFillModule.layerMinY);
        layerMaxField = field(cx + 55, y + 92, BoxFillModule.layerMaxY);

        addRenderableWidget(Button.builder(Component.literal("Crosshair -> corner 1"), b -> {
            BoxFillModule.pickCrosshairBlock().ifPresent(this::setCorner1);
        }).bounds(cx - 160, y + 116, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Crosshair -> corner 2"), b -> {
            BoxFillModule.pickCrosshairBlock().ifPresent(this::setCorner2);
        }).bounds(cx + 10, y + 116, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Fill region"), b -> {
            applyFields();
            BoxFillModule.placeCustomRegion();
        }).bounds(cx - 110, y + 142, 105, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Clear region"), b -> {
            applyFields();
            BoxFillModule.clearRegion();
        }).bounds(cx + 5, y + 142, 105, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Copy"), b -> BoxFillModule.copyRegion())
                .bounds(cx - 110, y + 168, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Paste"), b -> BoxFillModule.pasteRegion())
                .bounds(cx - 36, y + 168, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal(rotateLabel()), b -> {
            RegionClipboard.pasteRotation = (RegionClipboard.pasteRotation + 1) % 4;
            KazhiConfig.save();
            b.setMessage(Component.literal(rotateLabel()));
        }).bounds(cx + 38, y + 168, 72, 20).build());

        addRenderableWidget(Button.builder(Component.literal(mirrorXLabel()), b -> {
            RegionClipboard.mirrorX = !RegionClipboard.mirrorX;
            KazhiConfig.save();
            b.setMessage(Component.literal(mirrorXLabel()));
        }).bounds(cx - 110, y + 194, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal(mirrorZLabel()), b -> {
            RegionClipboard.mirrorZ = !RegionClipboard.mirrorZ;
            KazhiConfig.save();
            b.setMessage(Component.literal(mirrorZLabel()));
        }).bounds(cx - 36, y + 194, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Undo (U)"), b -> BoxFillModule.undoLastFill())
                .bounds(cx + 38, y + 194, 72, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Export"), b -> BoxFillModule.exportSchematic("selection"))
                .bounds(cx - 110, y + 220, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Import"), b -> BoxFillModule.importSchematic("selection"))
                .bounds(cx - 36, y + 220, 68, 20).build());
    }

    private EditBox field(int cx, int y, int value) {
        EditBox box = new EditBox(font, cx - 50, y, 100, 18, Component.literal(""));
        box.setValue(String.valueOf(value));
        addRenderableWidget(box);
        return box;
    }

    private void setCorner1(BlockPos pos) {
        x1Field.setValue(String.valueOf(pos.getX()));
        y1Field.setValue(String.valueOf(pos.getY()));
        z1Field.setValue(String.valueOf(pos.getZ()));
    }

    private void setCorner2(BlockPos pos) {
        x2Field.setValue(String.valueOf(pos.getX()));
        y2Field.setValue(String.valueOf(pos.getY()));
        z2Field.setValue(String.valueOf(pos.getZ()));
    }

    private static String hollowLabel() {
        return BoxFillModule.hollow ? "Mode: Hollow shell" : "Mode: Solid fill";
    }

    private static String airOnlyLabel() {
        return BoxFillModule.airOnly ? "Skip non-air" : "Replace all";
    }

    private static String replaceLabel() {
        return BoxFillModule.replaceMode ? "Replace: offhand filter" : "Replace mode: OFF";
    }

    private static String liquidLabel() {
        return switch (BoxFillModule.liquidFillMode) {
            case BoxFillModule.LIQUID_WATER -> "Liquid: Water";
            case BoxFillModule.LIQUID_LAVA -> "Liquid: Lava";
            default -> "Liquid: OFF";
        };
    }

    private static String outlineLabel() {
        return BoxFillModule.selectionOutline ? "Outline: ON" : "Outline: OFF";
    }

    private static String layerLabel() {
        return BoxFillModule.layerMode ? "Layer slice: ON" : "Layer slice: OFF";
    }

    private static String rotateLabel() {
        return "Rotate " + (RegionClipboard.pasteRotation * 90) + "°";
    }

    private static String mirrorXLabel() {
        return RegionClipboard.mirrorX ? "Mirror X: ON" : "Mirror X: OFF";
    }

    private static String mirrorZLabel() {
        return RegionClipboard.mirrorZ ? "Mirror Z: ON" : "Mirror Z: OFF";
    }

    private void applyFields() {
        try {
            if (xField != null) {
                BoxFillModule.centerX = Integer.parseInt(xField.getValue().trim());
                BoxFillModule.centerY = Integer.parseInt(yField.getValue().trim());
                BoxFillModule.centerZ = Integer.parseInt(zField.getValue().trim());
                BoxFillModule.radius = Integer.parseInt(radiusField.getValue().trim());
            }
            if (x1Field != null) {
                BoxFillModule.corner1X = Integer.parseInt(x1Field.getValue().trim());
                BoxFillModule.corner1Y = Integer.parseInt(y1Field.getValue().trim());
                BoxFillModule.corner1Z = Integer.parseInt(z1Field.getValue().trim());
                BoxFillModule.corner2X = Integer.parseInt(x2Field.getValue().trim());
                BoxFillModule.corner2Y = Integer.parseInt(y2Field.getValue().trim());
                BoxFillModule.corner2Z = Integer.parseInt(z2Field.getValue().trim());
            }
            if (layerMinField != null) {
                BoxFillModule.layerMinY = Integer.parseInt(layerMinField.getValue().trim());
                BoxFillModule.layerMaxY = Integer.parseInt(layerMaxField.getValue().trim());
            }
        } catch (NumberFormatException ignored) {
        }
        KazhiConfig.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.drawCenteredString(font, "World Fill (creative, no OP needed)", width / 2, 10, 0xFFE8E8FF);
        graphics.drawCenteredString(font, "Main hand = block | Offhand = replace filter | Layer Y: -1 = use region", width / 2, height - 40, 0xFF888888);

        if (tab == Tab.BOX) {
            graphics.drawString(font, "Center X / Y / Z / Radius", width / 2 - 110, 68, 0xFFAAAAAA);
            graphics.drawString(font, "Layer min Y", width / 2 - 105, 102, 0xFFAAAAAA);
            graphics.drawString(font, "Layer max Y", width / 2 + 5, 102, 0xFFAAAAAA);
        } else {
            graphics.drawString(font, "Corner 1", width / 2 - 105, 64, 0xFFAAAAFF);
            graphics.drawString(font, "Corner 2", width / 2 + 5, 64, 0xFFAAAAFF);
            graphics.drawString(font, "Layer min", width / 2 - 105, 82, 0xFFAAAAAA);
            graphics.drawString(font, "Layer max", width / 2 + 5, 82, 0xFFAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        applyFields();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
