package net.firetastesgood.ageofminecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

public class FusionChickenRenderer extends ChickenRenderer {
    public FusionChickenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void scale(Chicken entity, PoseStack pose, float partialTick) {
        super.scale(entity, pose, partialTick);

        if (entity.tickCount <= 21) {
            float base = (entity.tickCount + partialTick - 1.0F) / 20.0F * 1.6F;
            float t = Mth.sqrt(Math.max(base, 0.0F));
            if (t > 1.0F) t = 1.0F;
            pose.scale(t, t, t);
        }
    }

    @Override
    protected void setupRotations(Chicken entity, PoseStack pose, float ageInTicks, float rotationYaw, float partialTick) {
        super.setupRotations(entity, pose, ageInTicks, rotationYaw, partialTick);

        if (entity.tickCount <= 21) {
            float base = (entity.tickCount + partialTick - 1.0F) / 20.0F * 1.6F;
            float t = Mth.sqrt(Math.max(base, 0.0F));
            if (t > 1.0F) t = 1.0F;

            float angle = t * 90.0F - 90.0F;
            pose.mulPose(Axis.XP.rotationDegrees(angle));
        }
    }
}