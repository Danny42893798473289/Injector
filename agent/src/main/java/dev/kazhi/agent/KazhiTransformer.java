package dev.kazhi.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * Rewrites loaded Minecraft classes to call into dev.kazhi.rt.KazhiHooks.
 */
public final class KazhiTransformer implements ClassFileTransformer {
    private static final int API = Opcodes.ASM9;
    private static final String HOOKS = "dev/kazhi/rt/KazhiHooks";

    private static final String GAME_RENDERER = "net/minecraft/client/renderer/GameRenderer";
    private static final String LIGHT_TEXTURE = "net/minecraft/client/renderer/LightTexture";
    private static final String BLOCK = "net/minecraft/world/level/block/Block";
    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String GUI = "net/minecraft/client/gui/Gui";
    private static final String CAMERA = "net/minecraft/client/Camera";
    private static final String LOCAL_PLAYER = "net/minecraft/client/player/LocalPlayer";
    private static final String TICK_DESC = "()V";

    private static final String GET_FOV_DESC = "(Lnet/minecraft/client/Camera;FZ)F";
    private static final String BOB_HURT_DESC = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V";
    private static final String SHOULD_RENDER_FACE_DESC =
            "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;"
            + "Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z";
    private static final String RUN_TICK_DESC = "(Z)V";
    private static final String GET_BRIGHTNESS_DESC = "(FI)F";
    private static final String BLOCK_STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String GUI_RENDER_DESC =
            "(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V";
    private static final String HUD_RENDER_HOOK = "onHudRender";
    private static final String RENDER_LEVEL_DESC = "(Lnet/minecraft/client/DeltaTracker;)V";
    private static final String LEVEL_RENDERER = "net/minecraft/client/renderer/LevelRenderer";
    private static final String LEVEL_RENDER_LEVEL_DESC =
            "(Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/DeltaTracker;Z"
            + "Lnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;"
            + "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V";
    private static final String CAMERA_SETUP_DESC =
            "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V";
    private static final String CAMERA_TYPE = "Lnet/minecraft/client/Camera;";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) {
            return null;
        }
        if (!className.equals(GAME_RENDERER) && !className.equals(LIGHT_TEXTURE)
                && !className.equals(BLOCK) && !className.equals(MINECRAFT)
                && !className.equals(GUI) && !className.equals(CAMERA)
                && !className.equals(LOCAL_PLAYER)) {
            return null;
        }
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new SuperClassAwareWriter(reader, ClassWriter.COMPUTE_FRAMES, loader);
            ClassVisitor visitor = new Patcher(writer, className);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            AgentLog.log("Patched " + className);
            return writer.toByteArray();
        } catch (Throwable t) {
            AgentLog.error("Failed to patch " + className, t);
            return null;
        }
    }

    private static final class Patcher extends ClassVisitor {
        private final String owner;

        Patcher(ClassVisitor cv, String owner) {
            super(API, cv);
            this.owner = owner;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (mv == null) {
                return null;
            }
            if (owner.equals(GAME_RENDERER) && name.equals("getFov") && descriptor.equals(GET_FOV_DESC)) {
                return new FovMultiplier(mv);
            }
            if (owner.equals(GAME_RENDERER) && name.equals("bobHurt") && descriptor.equals(BOB_HURT_DESC)) {
                return new HeadGuard(mv, "noHurtCam");
            }
            if (owner.equals(BLOCK) && name.equals("shouldRenderFace") && descriptor.equals(SHOULD_RENDER_FACE_DESC)) {
                return new XRayFace(mv);
            }
            if (owner.equals(MINECRAFT) && name.equals("runTick") && descriptor.equals(RUN_TICK_DESC)) {
                return new HeadCall(mv, "onClientTick");
            }
            if (owner.equals(LIGHT_TEXTURE) && name.equals("getBrightness") && descriptor.equals(GET_BRIGHTNESS_DESC)) {
                return new FullbrightBrightness(mv);
            }
            if (owner.equals(GUI) && name.equals("render") && descriptor.equals(GUI_RENDER_DESC)) {
                return new HudRenderCall(mv);
            }
            if (owner.equals(GAME_RENDERER) && name.equals("renderLevel") && descriptor.equals(RENDER_LEVEL_DESC)) {
                return new AfterLevelRenderCall(mv);
            }
            if (owner.equals(CAMERA) && name.equals("setup") && descriptor.equals(CAMERA_SETUP_DESC)) {
                return new CameraTailCall(mv);
            }
            if (owner.equals(LOCAL_PLAYER) && name.equals("tick") && descriptor.equals(TICK_DESC)) {
                return new TailCall(mv, "onPlayerTickPost");
            }
            return mv;
        }
    }

    private static final class FullbrightBrightness extends MethodVisitor {
        FullbrightBrightness(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.FRETURN) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "applyFullbright", "(F)F", false);
            }
            super.visitInsn(opcode);
        }
    }

    private static final class FovMultiplier extends MethodVisitor {
        FovMultiplier(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.FRETURN) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "fovMul", "()F", false);
                super.visitInsn(Opcodes.FMUL);
            }
            super.visitInsn(opcode);
        }
    }

    /** Invoke a static hook with no args immediately before each RETURN. */
    /** Invoke onWorldRender immediately after LevelRenderer.renderLevel (world matrices still active). */
    private static final class AfterLevelRenderCall extends MethodVisitor {
        AfterLevelRenderCall(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (opcode == Opcodes.INVOKEVIRTUAL
                    && owner.equals(LEVEL_RENDERER)
                    && name.equals("renderLevel")
                    && descriptor.equals(LEVEL_RENDER_LEVEL_DESC)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "onWorldRender", "()V", false);
            }
        }
    }

    private static final class TailCall extends MethodVisitor {
        private final String method;

        TailCall(MethodVisitor mv, String method) {
            super(API, mv);
            this.method = method;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, method, "()V", false);
            }
            super.visitInsn(opcode);
        }
    }

    private static final class CameraTailCall extends MethodVisitor {
        CameraTailCall(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "dev/kazhi/rt/WorldRenderer",
                        "applyFreecam", "(" + CAMERA_TYPE + ")V", false);
            }
            super.visitInsn(opcode);
        }
    }

    private static final class HeadCall extends MethodVisitor {
        private final String method;

        HeadCall(MethodVisitor mv, String method) {
            super(API, mv);
            this.method = method;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, method, "()V", false);
        }
    }

    /** Gui#render: KazhiHooks.onHudRender(guiGraphics, deltaTracker) at head. */
    private static final class HudRenderCall extends MethodVisitor {
        HudRenderCall(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, HUD_RENDER_HOOK,
                    "(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", false);
        }
    }

    private static final class HeadGuard extends MethodVisitor {
        private final String method;

        HeadGuard(MethodVisitor mv, String method) {
            super(API, mv);
            this.method = method;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, method, "()Z", false);
            Label proceed = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, proceed);
            super.visitInsn(Opcodes.RETURN);
            super.visitLabel(proceed);
        }
    }

    private static final class XRayFace extends MethodVisitor {
        XRayFace(MethodVisitor mv) {
            super(API, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "xrayOverride", "(" + BLOCK_STATE + ")I", false);
            super.visitVarInsn(Opcodes.ISTORE, 5);

            super.visitVarInsn(Opcodes.ILOAD, 5);
            Label vanilla = new Label();
            super.visitJumpInsn(Opcodes.IFLT, vanilla);

            super.visitVarInsn(Opcodes.ILOAD, 5);
            Label returnFalse = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, returnFalse);
            super.visitInsn(Opcodes.ICONST_1);
            super.visitInsn(Opcodes.IRETURN);

            super.visitLabel(returnFalse);
            super.visitInsn(Opcodes.ICONST_0);
            super.visitInsn(Opcodes.IRETURN);

            super.visitLabel(vanilla);
        }
    }

    private static final class SuperClassAwareWriter extends ClassWriter {
        private final ClassLoader loader;

        SuperClassAwareWriter(ClassReader reader, int flags, ClassLoader loader) {
            super(reader, flags);
            this.loader = loader != null ? loader : getClass().getClassLoader();
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                Class<?> c1 = Class.forName(type1.replace('/', '.'), false, loader);
                Class<?> c2 = Class.forName(type2.replace('/', '.'), false, loader);
                if (c1.isAssignableFrom(c2)) {
                    return type1;
                }
                if (c2.isAssignableFrom(c1)) {
                    return type2;
                }
                if (c1.isInterface() || c2.isInterface()) {
                    return "java/lang/Object";
                }
                Class<?> c = c1;
                do {
                    c = c.getSuperclass();
                } while (c != null && !c.isAssignableFrom(c2));
                return c == null ? "java/lang/Object" : c.getName().replace('.', '/');
            } catch (Throwable t) {
                return "java/lang/Object";
            }
        }
    }
}
