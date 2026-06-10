package dev.kazhi.gui;

import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.Category;
import dev.kazhi.module.HasSettings;
import dev.kazhi.module.Module;
import dev.kazhi.module.ModuleManager;
import dev.kazhi.rt.KazhiHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends Screen {
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 290;
    private static final int SIDEBAR_W = 104;
    private static final int HEADER_H = 36;
    private static final int ROW_HEIGHT = 24;
    private static final int PADDING = 10;
    private static final int TOGGLE_W = 36;
    private static final int KEY_H = 16;

    private Category selectedCategory = Category.MOVEMENT;
    private Module bindingModule;
    private int scrollOffset;
    private EditBox searchBox;
    private String searchQuery = "";

    public ClickGuiScreen() {
        super(Component.literal("Kazhi"));
    }

    @Override
    protected void init() {
        layoutSearchBox();
        addRenderableWidget(searchBox);
        setFocused(searchBox);
    }

    private void layoutSearchBox() {
        int[] layout = panelLayout();
        int listX = layout[0] + SIDEBAR_W + PADDING;
        int listW = layout[2] - SIDEBAR_W - PADDING * 2 - 6;
        if (searchBox == null) {
            searchBox = new EditBox(font, listX, layout[1] + HEADER_H + 6, listW, 16, Component.literal("Search"));
            searchBox.setMaxLength(32);
            searchBox.setValue(searchQuery);
            searchBox.setResponder(s -> {
                searchQuery = s.toLowerCase(Locale.ROOT);
                scrollOffset = 0;
            });
        } else {
            searchBox.setX(listX);
            searchBox.setY(layout[1] + HEADER_H + 6);
            searchBox.setWidth(listW);
        }
    }

    private int[] panelLayout() {
        int x = width / 2 - PANEL_W / 2;
        int y = height / 2 - PANEL_H / 2;
        return new int[]{x, y, PANEL_W, PANEL_H};
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        try {
            renderContent(graphics, mouseX, mouseY);
        } catch (Throwable t) {
            dev.kazhi.rt.KazhiLog.error("ClickGUI render failed", t);
            onClose();
        }
    }

    private void renderContent(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiTheme theme = GuiTheme.current();
        int[] layout = panelLayout();
        int panelX = layout[0];
        int panelY = layout[1];
        int panelW = layout[2];
        int panelH = layout[3];

        graphics.fill(0, 0, width, height, theme.overlay);
        GuiDraw.panel(graphics, panelX, panelY, panelW, panelH, theme);
        GuiDraw.headerBar(graphics, panelX, panelY, panelW, HEADER_H, theme);

        int enabledCount = countEnabled();
        int totalCount = ModuleManager.get().getModules().size();
        graphics.drawString(font, "Kazhi", panelX + PADDING, panelY + 8, theme.textTitle);
        graphics.drawString(font, "v" + KazhiHooks.getVersion(), panelX + PADDING + font.width("Kazhi") + 6,
                panelY + 10, theme.textDim);
        String stats = enabledCount + "/" + totalCount + " active";
        int statsW = font.width(stats);
        graphics.drawString(font, stats, panelX + panelW - PADDING - statsW, panelY + 10, theme.enabled);

        renderSidebar(graphics, panelX, panelY, theme, mouseX, mouseY);
        renderModuleList(graphics, panelX, panelY, panelW, panelH, theme, mouseX, mouseY);
        renderThemePicker(graphics, panelX, panelY + panelH - 22, panelW, theme, mouseX, mouseY);
        renderFooter(graphics, panelX, panelY, panelW, panelH, theme);
    }

    private void renderSidebar(GuiGraphics graphics, int panelX, int panelY, GuiTheme theme, int mouseX, int mouseY) {
        int tabY = panelY + HEADER_H + 4;
        for (Category category : Category.values()) {
            int ty = tabY + category.ordinal() * 26;
            boolean selected = category == selectedCategory;
            boolean hover = mouseX >= panelX + 4 && mouseX < panelX + SIDEBAR_W - 4
                    && mouseY >= ty && mouseY < ty + 22;

            int bg = selected ? theme.tabActive : (hover ? theme.rowHover : theme.tabInactive);
            graphics.fill(panelX + 4, ty, panelX + SIDEBAR_W - 4, ty + 22, bg);
            if (selected) {
                GuiDraw.accentLine(graphics, panelX + 4, ty, 22, theme.categoryColor(category));
            }

            int catColor = theme.categoryColor(category);
            graphics.drawString(font, category.getDisplayName(), panelX + 14, ty + 7, selected ? theme.text : theme.textDim);

            int catEnabled = countEnabledIn(category);
            int catTotal = ModuleManager.get().getByCategory(category).size();
            String badge = catEnabled + "/" + catTotal;
            int badgeW = font.width(badge);
            graphics.drawString(font, badge, panelX + SIDEBAR_W - 8 - badgeW, ty + 7, catColor);
        }
    }

    private void renderModuleList(GuiGraphics graphics, int panelX, int panelY, int panelW, int panelH,
                                  GuiTheme theme, int mouseX, int mouseY) {
        int listX = panelX + SIDEBAR_W + PADDING;
        int listY = panelY + HEADER_H + 28;
        int listW = panelW - SIDEBAR_W - PADDING * 2 - 6;
        int listH = panelH - HEADER_H - 54;

        graphics.fill(listX, panelY + HEADER_H + 2, listX + listW, panelY + HEADER_H + 24, theme.searchBg);
        graphics.drawString(font, selectedCategory.getDisplayName(), listX + 4, panelY + HEADER_H + 4, theme.textTitle);

        if (searchBox != null) {
            layoutSearchBox();
            searchBox.render(graphics, mouseX, mouseY, 0);
        }

        List<Module> modules = filteredModules();
        int visibleRows = listH / ROW_HEIGHT;
        int maxScroll = Math.max(0, modules.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        Module hovered = null;
        for (int i = 0; i < visibleRows && i + scrollOffset < modules.size(); i++) {
            Module module = modules.get(i + scrollOffset);
            int rowY = listY + i * ROW_HEIGHT;
            boolean hover = mouseX >= listX && mouseX < listX + listW - 6
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2;
            if (hover) {
                hovered = module;
            }

            int rowColor = hover ? theme.rowHover : (i % 2 == 0 ? theme.row : theme.tabInactive);
            graphics.fill(listX, rowY, listX + listW - 6, rowY + ROW_HEIGHT - 2, rowColor);

            String name = module.getName();
            if (module instanceof HasSettings) {
                name += " *";
            }
            graphics.drawString(font, name, listX + 6, rowY + 4,
                    module.isEnabled() ? theme.text : theme.textDim);

            String keyLabel = bindingModule == module ? "..." : module.getKeyName();
            int keyW = Math.max(28, font.width(keyLabel) + 10);
            int keyX = listX + listW - 6 - keyW - TOGGLE_W - 8;
            GuiDraw.keyChip(graphics, font, keyX, rowY + 3, keyW, KEY_H, keyLabel, bindingModule == module, theme);

            int toggleX = listX + listW - 6 - TOGGLE_W - 2;
            GuiDraw.togglePill(graphics, font, toggleX, rowY + 3, TOGGLE_W, KEY_H, module.isEnabled(), theme);
        }

        GuiDraw.scrollbar(graphics, listX + listW - 3, listY, listH, modules.size(), visibleRows, scrollOffset, theme);

        if (hovered != null) {
            graphics.fill(listX, panelY + panelH - 38, listX + listW - 6, panelY + panelH - 24, theme.searchBg);
            graphics.drawString(font, hovered.getDescription(), listX + 4, panelY + panelH - 35, theme.textDim);
        }
    }

    private void renderThemePicker(GuiGraphics graphics, int panelX, int y, int panelW, GuiTheme theme,
                                   int mouseX, int mouseY) {
        graphics.drawString(font, "Theme", panelX + PADDING, y + 5, theme.textDim);
        int dotX = panelX + PADDING + font.width("Theme") + 10;
        int dotY = y + 9;
        for (GuiTheme candidate : GuiTheme.values()) {
            boolean selected = candidate == theme;
            boolean hover = Math.hypot(mouseX - dotX, mouseY - dotY) <= 7;
            if (hover && !selected) {
                graphics.fill(dotX - 6, dotY - 6, dotX + 6, dotY + 6, theme.rowHover);
            }
            GuiDraw.themeDot(graphics, dotX, dotY, 5, candidate, selected);
            dotX += 16;
        }
        String themeLabel = theme.label;
        graphics.drawString(font, themeLabel, panelX + panelW - PADDING - font.width(themeLabel), y + 5, theme.accent);
    }

    private void renderFooter(GuiGraphics graphics, int panelX, int panelY, int panelW, int panelH, GuiTheme theme) {
        if (bindingModule != null) {
            graphics.drawCenteredString(font,
                    "Binding " + bindingModule.getName() + " — press a key (ESC cancel, DEL clear)",
                    panelX + panelW / 2, panelY + panelH + 6, theme.accent);
        } else {
            graphics.drawCenteredString(font,
                    "LMB toggle · key chip bind · RMB * settings/HUD editor · scroll",
                    panelX + panelW / 2, panelY + panelH + 6, theme.textDim);
        }
    }

    private static int countEnabled() {
        int n = 0;
        for (Module module : ModuleManager.get().getModules()) {
            if (module.isEnabled()) {
                n++;
            }
        }
        return n;
    }

    private static int countEnabledIn(Category category) {
        int n = 0;
        for (Module module : ModuleManager.get().getByCategory(category)) {
            if (module.isEnabled()) {
                n++;
            }
        }
        return n;
    }

    private List<Module> filteredModules() {
        List<Module> all = ModuleManager.get().getByCategory(selectedCategory);
        if (searchQuery.isEmpty()) {
            return all;
        }
        List<Module> out = new ArrayList<>();
        for (Module module : all) {
            if (module.getName().toLowerCase(Locale.ROOT).contains(searchQuery)
                    || module.getDescription().toLowerCase(Locale.ROOT).contains(searchQuery)) {
                out.add(module);
            }
        }
        return out;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox != null && searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int[] layout = panelLayout();
        int panelX = layout[0];
        int panelY = layout[1];
        int panelW = layout[2];
        int panelH = layout[3];

        if (handleThemeClick(panelX, panelY + panelH - 22, mouseX, mouseY)) {
            return true;
        }

        int tabY = panelY + HEADER_H + 4;
        for (Category category : Category.values()) {
            int ty = tabY + category.ordinal() * 26;
            if (mouseX >= panelX + 4 && mouseX < panelX + SIDEBAR_W - 4
                    && mouseY >= ty && mouseY < ty + 22) {
                selectedCategory = category;
                scrollOffset = 0;
                return true;
            }
        }

        int listX = panelX + SIDEBAR_W + PADDING;
        int listY = panelY + HEADER_H + 28;
        int listW = panelW - SIDEBAR_W - PADDING * 2 - 6;
        int listH = panelH - HEADER_H - 54;
        List<Module> modules = filteredModules();
        int visibleRows = listH / ROW_HEIGHT;

        for (int i = 0; i < visibleRows && i + scrollOffset < modules.size(); i++) {
            Module module = modules.get(i + scrollOffset);
            int rowY = listY + i * ROW_HEIGHT;
            if (mouseX < listX || mouseX >= listX + listW - 6
                    || mouseY < rowY || mouseY >= rowY + ROW_HEIGHT - 2) {
                continue;
            }

            String keyLabel = bindingModule == module ? "..." : module.getKeyName();
            int keyW = Math.max(28, font.width(keyLabel) + 10);
            int toggleX = listX + listW - 6 - TOGGLE_W - 2;
            int keyX = listX + listW - 6 - keyW - TOGGLE_W - 8;

            if (button == 1 && module instanceof HasSettings settings) {
                minecraft.setScreen(settings.createSettingsScreen(minecraft, this));
                return true;
            }
            if (mouseX >= keyX && mouseX < keyX + keyW) {
                bindingModule = module;
                return true;
            }
            if (mouseX >= toggleX) {
                module.toggle();
                KazhiConfig.save();
            } else {
                module.toggle();
                KazhiConfig.save();
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleThemeClick(int panelX, int y, double mouseX, double mouseY) {
        int dotX = panelX + PADDING + font.width("Theme") + 10;
        int dotY = y + 9;
        int index = 0;
        for (GuiTheme ignored : GuiTheme.values()) {
            if (Math.hypot(mouseX - dotX, mouseY - dotY) <= 7) {
                GuiTheme.setCurrent(index);
                KazhiConfig.save();
                return true;
            }
            dotX += 16;
            index++;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int[] layout = panelLayout();
        int listX = layout[0] + SIDEBAR_W + PADDING;
        if (mouseX >= listX) {
            scrollOffset = Math.max(0, scrollOffset - (int) scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
            } else if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.setKeyCode(0);
                bindingModule = null;
                KazhiConfig.save();
            } else {
                bindingModule.setKeyCode(keyCode);
                bindingModule = null;
                KazhiConfig.save();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        bindingModule = null;
        if (searchBox != null) {
            searchQuery = searchBox.getValue().toLowerCase(Locale.ROOT);
        }
        KazhiConfig.save();
        super.onClose();
    }
}
