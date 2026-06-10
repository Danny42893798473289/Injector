package dev.kazhi.config;

public final class KazhiInput {
    public static int menuKey = KazhiKeys.DEFAULT_MENU;
    public static int zoomKey = KazhiKeys.DEFAULT_ZOOM;
    public static int panicKey = KazhiKeys.DEFAULT_PANIC;
    public static int pos1Key = KazhiKeys.DEFAULT_POS1;
    public static int pos2Key = KazhiKeys.DEFAULT_POS2;

    private KazhiInput() {}

    public static void apply(KazhiConfig config) {
        if (config == null) {
            return;
        }
        menuKey = config.menuKeyCode != 0 ? config.menuKeyCode : KazhiKeys.DEFAULT_MENU;
        zoomKey = config.zoomKeyCode != 0 ? config.zoomKeyCode : KazhiKeys.DEFAULT_ZOOM;
        panicKey = config.panicKeyCode != 0 ? config.panicKeyCode : KazhiKeys.DEFAULT_PANIC;
        pos1Key = config.pos1KeyCode != 0 ? config.pos1KeyCode : KazhiKeys.DEFAULT_POS1;
        pos2Key = config.pos2KeyCode != 0 ? config.pos2KeyCode : KazhiKeys.DEFAULT_POS2;
    }

    public static void writeTo(KazhiConfig config) {
        config.menuKeyCode = menuKey;
        config.zoomKeyCode = zoomKey;
        config.panicKeyCode = panicKey;
        config.pos1KeyCode = pos1Key;
        config.pos2KeyCode = pos2Key;
    }
}
