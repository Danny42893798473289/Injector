package dev.kazhi.gui;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.impl.XRayModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class XRayScreen extends Screen {
    private final Screen parent;

    public XRayScreen(Screen parent) {
        super(Component.literal("XRay Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int cx = width / 2;
        int y = 40;
        y = oreToggle(cx, y, "Coal", XRayModule::isCoalEnabled, XRayModule::setCoalEnabled);
        y = oreToggle(cx, y, "Iron", XRayModule::isIronEnabled, XRayModule::setIronEnabled);
        y = oreToggle(cx, y, "Copper", XRayModule::isCopperEnabled, XRayModule::setCopperEnabled);
        y = oreToggle(cx, y, "Gold", XRayModule::isGoldEnabled, XRayModule::setGoldEnabled);
        y = oreToggle(cx, y, "Redstone", XRayModule::isRedstoneEnabled, XRayModule::setRedstoneEnabled);
        y = oreToggle(cx, y, "Lapis", XRayModule::isLapisEnabled, XRayModule::setLapisEnabled);
        y = oreToggle(cx, y, "Diamond", XRayModule::isDiamondEnabled, XRayModule::setDiamondEnabled);
        y = oreToggle(cx, y, "Emerald", XRayModule::isEmeraldEnabled, XRayModule::setEmeraldEnabled);
        y = oreToggle(cx, y, "Ancient debris", XRayModule::isAncientDebrisEnabled, XRayModule::setAncientDebrisEnabled);
        y = oreToggle(cx, y, "Nether gold", XRayModule::isNetherGoldEnabled, XRayModule::setNetherGoldEnabled);
        y = oreToggle(cx, y, "Nether quartz", XRayModule::isNetherQuartzEnabled, XRayModule::setNetherQuartzEnabled);

        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 40, height - 32, 80, 20).build());
    }

    private int oreToggle(int cx, int y, String name,
                          java.util.function.BooleanSupplier getter,
                          java.util.function.Consumer<Boolean> setter) {
        boolean on = getter.getAsBoolean();
        addRenderableWidget(Button.builder(Component.literal(name + ": " + (on ? "ON" : "OFF")), b -> {
            setter.accept(!getter.getAsBoolean());
            KazhiConfig.save();
            init();
        }).bounds(cx - 110, y, 220, 20).build());
        return y + 22;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.drawCenteredString(font, "XRay — ore visibility", width / 2, 12, 0xFFE8E8FF);
        graphics.drawCenteredString(font, "Extra blocks: edit settings.json xrayExtraBlocks", width / 2, 24, 0xFF888888);
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
