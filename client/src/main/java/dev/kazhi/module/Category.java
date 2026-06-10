package dev.kazhi.module;

public enum Category {
    MOVEMENT("Movement"),
    RENDER("Render"),
    BUILD("Build"),
    MISC("Misc"),
    STRESS("Stress Test"),
    CLIENT("Client");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
