package dev.kazhi.config;

import org.lwjgl.glfw.GLFW;

public final class KazhiKeys {
    public static final int DEFAULT_MENU = GLFW.GLFW_KEY_INSERT;
    public static final int DEFAULT_ZOOM = GLFW.GLFW_KEY_C;
    public static final int DEFAULT_PANIC = GLFW.GLFW_KEY_END;
    public static final int DEFAULT_POS1 = GLFW.GLFW_KEY_LEFT_BRACKET;
    public static final int DEFAULT_POS2 = GLFW.GLFW_KEY_RIGHT_BRACKET;

    private KazhiKeys() {}

    public static String name(int keyCode) {
        if (keyCode == 0 || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "None";
        }
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name != null ? name.toUpperCase() : "Key " + keyCode;
    }
}
