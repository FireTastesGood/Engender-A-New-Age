package net.firetastesgood.ageofminecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.firetastesgood.ageofminecraft.entity.EntropyOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class EntropyOrbRenderer extends EntityRenderer<EntropyOrbEntity> {
    private static final ResourceLocation XP_TEX = new ResourceLocation("minecraft", "textures/entity/experience_orb.png");

    private static final float TEX_SIZE = 64f;
    private static final float TILE     = 16f;

    private static final float BASE_SCALE   = 0.18f;
    private static final float PER_TIER_ADD = 0.06f;

    private static final float PULSE_SPEED = 6.0f;

    private static final float R0 = 1.00f, G0 = 0.20f, B0 = 0.20f;
    private static final float R1 = 1.00f, G1 = 0.45f, B1 = 0.20f;

    public EntropyOrbRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius   = 0.03F;
        this.shadowStrength = 0.5F;
    }

    @Override
    public void render(EntropyOrbEntity orb, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buf, int packedLight) {
        pose.pushPose();

        pose.translate(0.0D, 0.15F, 0.0D);
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));

        int sizeIndex = orb.getOrbSizeIndex();
        float scale   = BASE_SCALE + (sizeIndex * PER_TIER_ADD);
        pose.scale(scale, scale, scale);

        int tileRow = Math.min(3, sizeIndex / 3);
        int tileCol = 0;

        float u0 = (tileCol * TILE) / TEX_SIZE;
        float v0 = (tileRow * TILE) / TEX_SIZE;
        float u1 = ((tileCol * TILE) + TILE) / TEX_SIZE;
        float v1 = ((tileRow * TILE) + TILE) / TEX_SIZE;

        float t = 0.5f + 0.5f * (float)Math.sin((orb.tickCount + partialTicks + (orb.getId() & 7)) / PULSE_SPEED);
        float r = lerp(R0, R1, t), g = lerp(G0, G1, t), b = lerp(B0, B1, t), aCol = 1.0f;

        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(XP_TEX));

        pose.translate(-0.5D, -0.5D, 0.0D);
        PoseStack.Pose last = pose.last();
        Matrix4f poseMat = last.pose();
        Matrix3f normalMat = last.normal();

        // front
        addVertex(vc, poseMat, normalMat, 1, 0, 0, u1, v0, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 0, 0, 0, u0, v0, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 0, 1, 0, u0, v1, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 1, 1, 0, u1, v1, packedLight, r, g, b, aCol);

        // back
        addVertex(vc, poseMat, normalMat, 1, 1, 0, u1, v1, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 0, 1, 0, u0, v1, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 0, 0, 0, u0, v0, packedLight, r, g, b, aCol);
        addVertex(vc, poseMat, normalMat, 1, 0, 0, u1, v0, packedLight, r, g, b, aCol);

        pose.popPose();
        super.render(orb, entityYaw, partialTicks, pose, buf, packedLight);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static void addVertex(VertexConsumer vc, Matrix4f pose, Matrix3f normal,
                                  float x, float y, float z, float u, float v, int light,
                                  float r, float g, float b, float a) {
        vc.vertex(pose, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntropyOrbEntity entity) {
        return XP_TEX;
    }
}