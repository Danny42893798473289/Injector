package dev.kazhi.rt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kazhi.config.KazhiConfig;
import dev.kazhi.module.impl.BoxFillModule;
import dev.kazhi.module.impl.ChunkGridModule;
import dev.kazhi.module.impl.FreecamModule;
import dev.kazhi.module.impl.MobEspModule;
import dev.kazhi.module.impl.StorageEspModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WorldRenderer {
    private WorldRenderer() {}

    public static void render() {
        try {
            renderSafe();
        } catch (Throwable t) {
            KazhiLog.error("World render overlay error", t);
        }
    }

    private static void renderSafe() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack pose = new PoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        VertexConsumer espLines = buffers.getBuffer(RenderType.secondaryBlockOutline());
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        if (ChunkGridModule.isActive()) {
            renderChunkGrid(mc, pose, lines);
        }
        if (StorageEspModule.isActive()) {
            renderStorageEsp(mc, pose, espLines);
        }
        if (MobEspModule.isActive()) {
            renderMobEsp(mc, pose, espLines);
        }
        if (shouldRenderSelectionOutline()) {
            renderSelectionOutline(pose, lines);
        }

        pose.popPose();
        buffers.endBatch(RenderType.lines());
        buffers.endBatch(RenderType.secondaryBlockOutline());
    }

    private static boolean shouldRenderSelectionOutline() {
        if (!KazhiConfig.boxFillPreview && !BoxFillModule.selectionOutline) {
            return false;
        }
        return BoxFillModule.previewRegionMode || BoxFillModule.hasValidRegion();
    }

    private static void renderSelectionOutline(PoseStack pose, VertexConsumer lines) {
        int minX = Math.min(BoxFillModule.corner1X, BoxFillModule.corner2X);
        int minY = Math.min(BoxFillModule.corner1Y, BoxFillModule.corner2Y);
        int minZ = Math.min(BoxFillModule.corner1Z, BoxFillModule.corner2Z);
        int maxX = Math.max(BoxFillModule.corner1X, BoxFillModule.corner2X) + 1;
        int maxY = Math.max(BoxFillModule.corner1Y, BoxFillModule.corner2Y) + 1;
        int maxZ = Math.max(BoxFillModule.corner1Z, BoxFillModule.corner2Z) + 1;
        if (!BoxFillModule.hasValidRegion()) {
            int r = Math.max(0, BoxFillModule.radius);
            minX = BoxFillModule.centerX - r;
            minY = BoxFillModule.centerY - r;
            minZ = BoxFillModule.centerZ - r;
            maxX = BoxFillModule.centerX + r + 1;
            maxY = BoxFillModule.centerY + r + 1;
            maxZ = BoxFillModule.centerZ + r + 1;
        }
        ShapeRenderer.renderLineBox(pose, lines, minX, minY, minZ, maxX, maxY, maxZ, 0.9F, 0.4F, 1.0F, 0.95F);
    }

    private static void renderChunkGrid(Minecraft mc, PoseStack pose, VertexConsumer lines) {
        BlockPos playerPos = mc.player.blockPosition();
        int pcx = SectionPos.blockToSectionCoord(playerPos.getX());
        int pcz = SectionPos.blockToSectionCoord(playerPos.getZ());
        int minY = mc.level.getMinY();
        int maxY = mc.level.getMaxY();

        for (int cx = pcx - 2; cx <= pcx + 2; cx++) {
            for (int cz = pcz - 2; cz <= pcz + 2; cz++) {
                int x = SectionPos.sectionToBlockCoord(cx);
                int z = SectionPos.sectionToBlockCoord(cz);
                ShapeRenderer.renderLineBox(pose, lines, x, minY, z, x + 16, maxY, z + 16, 0.2F, 0.8F, 1.0F, 0.8F);
            }
        }
    }

    private static void renderStorageEsp(Minecraft mc, PoseStack pose, VertexConsumer lines) {
        BlockPos center = mc.player.blockPosition();
        int radius = StorageEspModule.radius;
        int radiusSq = radius * radius;
        int chunkRadius = (radius >> 4) + 1;
        int pcx = center.getX() >> 4;
        int pcz = center.getZ() >> 4;

        for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
            for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                if (chunk.isEmpty()) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!StorageEspTypes.isStorage(be)) {
                        continue;
                    }
                    BlockPos pos = be.getBlockPos();
                    if (pos.distSqr(center) > radiusSq) {
                        continue;
                    }
                    AABB box = new AABB(pos).inflate(0.002);
                    StorageEspTypes.Color color = StorageEspTypes.colorFor(be);
                    ShapeRenderer.renderLineBox(pose, lines, box, color.r(), color.g(), color.b(), color.a());
                }
            }
        }
    }

    private static void renderMobEsp(Minecraft mc, PoseStack pose, VertexConsumer lines) {
        int radius = MobEspModule.radius;
        double radiusSq = (double) radius * radius;
        Vec3 playerPos = mc.player.position();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player) {
                continue;
            }
            if (entity.position().distanceToSqr(playerPos) > radiusSq) {
                continue;
            }
            AABB box = entity.getBoundingBox().inflate(0.05);
            float r = living.isBaby() ? 0.4F : 0.9F;
            float g = 0.35F;
            float b = 0.95F;
            if (living.isInvisible()) {
                r *= 0.5F;
                g *= 0.5F;
                b *= 0.5F;
            }
            ShapeRenderer.renderLineBox(pose, lines, box, r, g, b, 0.9F);
        }
    }

    public static void applyFreecam(net.minecraft.client.Camera camera) {
        if (!FreecamModule.isActive()) {
            return;
        }
        try {
            var posField = net.minecraft.client.Camera.class.getDeclaredField("position");
            posField.setAccessible(true);
            posField.set(camera, FreecamModule.cameraPos);
            var xRotField = net.minecraft.client.Camera.class.getDeclaredField("xRot");
            var yRotField = net.minecraft.client.Camera.class.getDeclaredField("yRot");
            xRotField.setAccessible(true);
            yRotField.setAccessible(true);
            xRotField.setFloat(camera, FreecamModule.cameraPitch);
            yRotField.setFloat(camera, FreecamModule.cameraYaw);
        } catch (Throwable t) {
            KazhiLog.error("Freecam camera apply failed", t);
        }
    }
}
