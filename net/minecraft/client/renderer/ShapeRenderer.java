package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ShapeRenderer {
    public static void renderShape(
        PoseStack p_361053_, VertexConsumer p_364779_, VoxelShape p_362125_, double p_360355_, double p_363490_, double p_360995_, int p_380036_
    ) {
        PoseStack.Pose posestack$pose = p_361053_.last();
        p_362125_.forAllEdges(
            (p_365487_, p_362038_, p_363457_, p_361045_, p_364563_, p_362830_) -> {
                Vector3f vector3f = new Vector3f((float)(p_361045_ - p_365487_), (float)(p_364563_ - p_362038_), (float)(p_362830_ - p_363457_)).normalize();
                p_364779_.addVertex(posestack$pose, (float)(p_365487_ + p_360355_), (float)(p_362038_ + p_363490_), (float)(p_363457_ + p_360995_))
                    .setColor(p_380036_)
                    .setNormal(posestack$pose, vector3f);
                p_364779_.addVertex(posestack$pose, (float)(p_361045_ + p_360355_), (float)(p_364563_ + p_363490_), (float)(p_362830_ + p_360995_))
                    .setColor(p_380036_)
                    .setNormal(posestack$pose, vector3f);
            }
        );
    }

    public static void renderLineBox(
        PoseStack p_362266_, VertexConsumer p_363209_, AABB p_360496_, float p_360487_, float p_364782_, float p_364984_, float p_362275_
    ) {
        renderLineBox(
            p_362266_,
            p_363209_,
            p_360496_.minX,
            p_360496_.minY,
            p_360496_.minZ,
            p_360496_.maxX,
            p_360496_.maxY,
            p_360496_.maxZ,
            p_360487_,
            p_364782_,
            p_364984_,
            p_362275_,
            p_360487_,
            p_364782_,
            p_364984_
        );
    }

    public static void renderLineBox(
        PoseStack p_362875_,
        VertexConsumer p_363124_,
        double p_363833_,
        double p_364766_,
        double p_361321_,
        double p_363898_,
        double p_364838_,
        double p_362511_,
        float p_364758_,
        float p_361439_,
        float p_363962_,
        float p_361434_
    ) {
        renderLineBox(
            p_362875_,
            p_363124_,
            p_363833_,
            p_364766_,
            p_361321_,
            p_363898_,
            p_364838_,
            p_362511_,
            p_364758_,
            p_361439_,
            p_363962_,
            p_361434_,
            p_364758_,
            p_361439_,
            p_363962_
        );
    }

    public static void renderLineBox(
        PoseStack p_361633_,
        VertexConsumer p_362155_,
        double p_363670_,
        double p_363313_,
        double p_360539_,
        double p_361143_,
        double p_360876_,
        double p_362174_,
        float p_360474_,
        float p_362269_,
        float p_363641_,
        float p_361868_,
        float p_364627_,
        float p_360384_,
        float p_363766_
    ) {
        PoseStack.Pose posestack$pose = p_361633_.last();
        float f = (float)p_363670_;
        float f1 = (float)p_363313_;
        float f2 = (float)p_360539_;
        float f3 = (float)p_361143_;
        float f4 = (float)p_360876_;
        float f5 = (float)p_362174_;
        p_362155_.addVertex(posestack$pose, f, f1, f2).setColor(p_360474_, p_360384_, p_363766_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f2).setColor(p_360474_, p_360384_, p_363766_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f1, f2).setColor(p_364627_, p_362269_, p_363766_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f2).setColor(p_364627_, p_362269_, p_363766_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f1, f2).setColor(p_364627_, p_360384_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_362155_.addVertex(posestack$pose, f, f1, f5).setColor(p_364627_, p_360384_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, -1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, -1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f1, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f, f1, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, -1.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, -1.0F);
        p_362155_.addVertex(posestack$pose, f, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f1, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f2).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_362155_.addVertex(posestack$pose, f3, f4, f5).setColor(p_360474_, p_362269_, p_363641_, p_361868_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
    }

    public static void addChainedFilledBoxVertices(
        PoseStack p_363330_,
        VertexConsumer p_363261_,
        double p_360289_,
        double p_364864_,
        double p_362702_,
        double p_362652_,
        double p_360937_,
        double p_365101_,
        float p_364932_,
        float p_363659_,
        float p_363710_,
        float p_364890_
    ) {
        addChainedFilledBoxVertices(
            p_363330_,
            p_363261_,
            (float)p_360289_,
            (float)p_364864_,
            (float)p_362702_,
            (float)p_362652_,
            (float)p_360937_,
            (float)p_365101_,
            p_364932_,
            p_363659_,
            p_363710_,
            p_364890_
        );
    }

    public static void addChainedFilledBoxVertices(
        PoseStack p_360433_,
        VertexConsumer p_364868_,
        float p_360893_,
        float p_361291_,
        float p_361737_,
        float p_364738_,
        float p_362626_,
        float p_362721_,
        float p_361437_,
        float p_365290_,
        float p_363727_,
        float p_361847_
    ) {
        Matrix4f matrix4f = p_360433_.last().pose();
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_361291_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_360893_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_361737_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
        p_364868_.addVertex(matrix4f, p_364738_, p_362626_, p_362721_).setColor(p_361437_, p_365290_, p_363727_, p_361847_);
    }

    public static void renderFace(
        PoseStack p_364593_,
        VertexConsumer p_364615_,
        Direction p_362455_,
        float p_360745_,
        float p_364472_,
        float p_361704_,
        float p_362107_,
        float p_362819_,
        float p_362959_,
        float p_364620_,
        float p_362074_,
        float p_363881_,
        float p_360670_
    ) {
        Matrix4f matrix4f = p_364593_.last().pose();
        switch (p_362455_) {
            case DOWN:
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                break;
            case UP:
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                break;
            case NORTH:
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                break;
            case SOUTH:
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                break;
            case WEST:
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_360745_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                break;
            case EAST:
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_361704_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_362819_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
                p_364615_.addVertex(matrix4f, p_362107_, p_364472_, p_362959_).setColor(p_364620_, p_362074_, p_363881_, p_360670_);
        }
    }

    public static void renderVector(PoseStack p_360595_, VertexConsumer p_363463_, Vector3f p_360401_, Vec3 p_364678_, int p_360349_) {
        PoseStack.Pose posestack$pose = p_360595_.last();
        p_363463_.addVertex(posestack$pose, p_360401_)
            .setColor(p_360349_)
            .setNormal(posestack$pose, (float)p_364678_.x, (float)p_364678_.y, (float)p_364678_.z);
        p_363463_.addVertex(posestack$pose, (float)(p_360401_.x() + p_364678_.x), (float)(p_360401_.y() + p_364678_.y), (float)(p_360401_.z() + p_364678_.z))
            .setColor(p_360349_)
            .setNormal(posestack$pose, (float)p_364678_.x, (float)p_364678_.y, (float)p_364678_.z);
    }
}
