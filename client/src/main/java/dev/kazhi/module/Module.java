package dev.kazhi.module;

import org.lwjgl.glfw.GLFW;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyCode;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyCode = GLFW.GLFW_KEY_UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public void onTick() {}

    public String getKeyName() {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode == 0) {
            return "None";
        }
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name != null ? name.toUpperCase() : "Key " + keyCode;
    }
}
