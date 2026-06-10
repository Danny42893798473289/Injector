package dev.kazhi.module.impl;

import dev.kazhi.module.Category;
import dev.kazhi.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class FreecamModule extends Module {
    private static FreecamModule instance;
    public static Vec3 cameraPos = Vec3.ZERO;
    public static float cameraYaw;
    public static float cameraPitch;
    private static Vec3 savedPlayerPos = Vec3.ZERO;
    private static boolean captured;

    public FreecamModule() {
        super("Freecam", "Detach camera from the player", Category.RENDER);
        instance = this;
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        savedPlayerPos = player.position();
        cameraPos = player.position();
        cameraYaw = player.getYRot();
        cameraPitch = player.getXRot();
        captured = true;
        player.noPhysics = true;
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.noPhysics = false;
            if (captured) {
                player.setPos(savedPlayerPos);
            }
        }
        captured = false;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        player.setPos(savedPlayerPos);
        player.setDeltaMovement(Vec3.ZERO);

        long window = mc.getWindow().getWindow();
        float speed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ? 1.5F : 0.5F;
        double yawRad = Math.toRadians(cameraYaw);
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);

        Vec3 move = Vec3.ZERO;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            move = move.add(forwardX * speed, 0, forwardZ * speed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            move = move.add(-forwardX * speed, 0, -forwardZ * speed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            move = move.add(-rightX * speed, 0, -rightZ * speed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            move = move.add(rightX * speed, 0, rightZ * speed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            move = move.add(0, speed, 0);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS) {
            move = move.add(0, -speed, 0);
        }
        cameraPos = cameraPos.add(move);
        cameraYaw = player.getYRot();
        cameraPitch = player.getXRot();
    }
}
