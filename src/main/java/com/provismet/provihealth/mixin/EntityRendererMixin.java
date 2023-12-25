package com.provismet.provihealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.TargetHealthBar;
import com.provismet.provihealth.world.EntityHealthBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method="renderLabelIfPresent", at=@At("HEAD"), cancellable=true)
    private void cancelLabel (Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if (TargetHealthBar.disabledLabels || (Options.overrideLabels && entity instanceof LivingEntity living && Options.shouldRenderHealthFor(living))) info.cancel();
    }

    @Inject(method="render", at=@At("HEAD"))
    private void addHealthBar (Entity entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo info) {
        EntityHealthBar.render(entity, tickDelta, matrixStack, vertexConsumerProvider, MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
    }
}
