package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.module.modules.render.ItemPhysics;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class MixinItem {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void customSubmit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        ItemPhysics itemPhysics = Argon.INSTANCE.getModuleManager().getModule(ItemPhysics.class);
        if (itemPhysics == null || !itemPhysics.isEnabled()) return;
        if (state.item.isEmpty()) return;
        poseStack.pushPose();
        AABB box = state.item.getModelBoundingBox();
        float minOffsetY = -((float) box.minY) + 0.0625F;

        float bob = 0.0F;
        if (itemPhysics.bounce.getValue()) {
            bob = Mth.sin((state.ageInTicks / 10.0F + state.bobOffset)) * 0.1F + 0.1F;
        }

        poseStack.translate(0.0F, bob + minOffsetY, 0.0F);

        switch (itemPhysics.facingMode.getMode()) {
            case player:
                poseStack.mulPose(camera.orientation);
                break;

            case flat:
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                break;

            case none:
                break;
        }

        float time = (state.ageInTicks) * itemPhysics.rotationSpeed.getValueFloat();

        switch (itemPhysics.rotationAxis.getMode()) {
            case x:
                poseStack.mulPose(Axis.XP.rotationDegrees(time));
                break;

            case y:
                poseStack.mulPose(Axis.YP.rotationDegrees(time));
                break;

            case z:
                poseStack.mulPose(Axis.ZP.rotationDegrees(time));
                break;

            case none:
                break;
        }

        float scale = itemPhysics.scale.getValueFloat();
        poseStack.scale(scale, scale, scale);

        ItemEntityRenderer.submitMultipleFromCount(
                poseStack,
                collector,
                state.lightCoords,
                state,
                RandomSource.create(),
                box
        );
        state.outlineColor = 1;

        poseStack.popPose();

        //removes the item pickup animation but shouldnt do that!!
        ci.cancel();
    }
}