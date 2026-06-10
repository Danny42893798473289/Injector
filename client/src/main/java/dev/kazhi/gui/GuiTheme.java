package dev.kazhi.gui;

public enum GuiTheme {
    MIDNIGHT("Midnight", c(
            0x99000000, 0xF0121218, 0xFF2A2A38, 0xFF1A1A24,
            0xFF5B7CFA, 0xFF3D52A8, 0xFF1E1E2A, 0xFF2E3350,
            0xFF181820, 0xFF222230, 0xFFE8E8F0, 0xFF9090A8, 0xFFB8C0FF,
            0xFF6EE7A0, 0xFF606878, 0xFF353550, 0xFF1A1A22)),
    OCEAN("Ocean", c(
            0x99000812, 0xF00A141C, 0xFF1A3040, 0xFF0E1E28,
            0xFF3ECFCC, 0xFF2A9898, 0xFF122028, 0xFF1A3848,
            0xFF101820, 0xFF1A2838, 0xFFE0F4F4, 0xFF80A8A8, 0xFFA0E8E8,
            0xFF5EEDC0, 0xFF507070, 0xFF1E3848, 0xFF0E1820)),
    EMERALD("Emerald", c(
            0x99000808, 0xF00C1410, 0xFF1A3028, 0xFF0E1E18,
            0xFF4ADE80, 0xFF2A9858, 0xFF122018, 0xFF1A3828,
            0xFF101A14, 0xFF1A2E22, 0xFFE0F0E4, 0xFF80A090, 0xFFA8E8C0,
            0xFF86EFAC, 0xFF507060, 0xFF1E3828, 0xFF0E1814)),
    SUNSET("Sunset", c(
            0x990C0400, 0xF014100C, 0xFF403020, 0xFF281C14,
            0xFFFF8C42, 0xFFC06830, 0xFF281C14, 0xFF403028,
            0xFF1A1410, 0xFF302418, 0xFFFFF0E0, 0xFFA89080, 0xFFFFC8A0,
            0xFFFFB86C, 0xFF806858, 0xFF403028, 0xFF1A1410)),
    ROSE("Rose", c(
            0x990C0008, 0xF0140C10, 0xFF402030, 0xFF28141C,
            0xFFFF6B9D, 0xFFC04870, 0xFF28141C, 0xFF402030,
            0xFF1A1014, 0xFF302024, 0xFFFFF0F4, 0xFFA88898, 0xFFFFB0C8,
            0xFFFF8CB0, 0xFF806070, 0xFF402830, 0xFF1A1014)),
    AMETHYST("Amethyst", c(
            0x99080014, 0xF0100C18, 0xFF382040, 0xFF201428,
            0xFFC084FC, 0xFF8850B0, 0xFF201428, 0xFF382848,
            0xFF18101C, 0xFF2C2038, 0xFFF0E8FF, 0xFF9888A8, 0xFFD8B8FF,
            0xFFE879F9, 0xFF706080, 0xFF382848, 0xFF18101C)),
    SNOW("Snow", c(
            0x88E8E8F0, 0xF0F8F8FC, 0xFFD0D4DC, 0xFFE8ECF4,
            0xFF4A6CF5, 0xFF8090C0, 0xFFE8ECF4, 0xFFD8DEE8,
            0xFFF0F2F8, 0xFFE0E4EC, 0xFF282830, 0xFF686878, 0xFF4048A0,
            0xFF22A85A, 0xFF909098, 0xFFD0D4DC, 0xFFE8ECF4));

    private static final int COLOR_COUNT = 17;
    private static GuiTheme current = MIDNIGHT;

    public final String label;
    public final int overlay;
    public final int panelBg;
    public final int panelBorder;
    public final int headerBg;
    public final int accent;
    public final int accentDim;
    public final int tabInactive;
    public final int tabActive;
    public final int row;
    public final int rowHover;
    public final int text;
    public final int textDim;
    public final int textTitle;
    public final int enabled;
    public final int disabled;
    public final int keyBg;
    public final int searchBg;

    GuiTheme(String label, int[] colors) {
        if (colors.length != COLOR_COUNT) {
            throw new IllegalArgumentException(label + " needs " + COLOR_COUNT + " colors, got " + colors.length);
        }
        this.label = label;
        this.overlay = colors[0];
        this.panelBg = colors[1];
        this.panelBorder = colors[2];
        this.headerBg = colors[3];
        this.accent = colors[4];
        this.accentDim = colors[5];
        this.tabInactive = colors[6];
        this.tabActive = colors[7];
        this.row = colors[8];
        this.rowHover = colors[9];
        this.text = colors[10];
        this.textDim = colors[11];
        this.textTitle = colors[12];
        this.enabled = colors[13];
        this.disabled = colors[14];
        this.keyBg = colors[15];
        this.searchBg = colors[16];
    }

    private static int[] c(int... colors) {
        return colors;
    }

    public static GuiTheme current() {
        return current;
    }

    public static void setCurrent(int index) {
        GuiTheme[] values = values();
        if (index >= 0 && index < values.length) {
            current = values[index];
        }
    }

    public static void cycle() {
        current = values()[(current.ordinal() + 1) % values().length];
    }

    public int categoryColor(dev.kazhi.module.Category category) {
        return switch (category) {
            case MOVEMENT -> blend(accent, 0xFF60A5FA, 0.35F);
            case RENDER -> blend(accent, 0xFFC084FC, 0.35F);
            case BUILD -> blend(accent, 0xFF4ADE80, 0.35F);
            case MISC -> blend(accent, 0xFFFBBF24, 0.35F);
            case STRESS -> blend(accent, 0xFFAA55FF, 0.35F);
            case CLIENT -> blend(accent, 0xFF94A3B8, 0.35F);
        };
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
