package com.provismet.provihealth.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.particle.TextParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @SuppressWarnings("resource")
    @Inject(method="renderParticles", at=@At(value="INVOKE", target="Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V", shift=At.Shift.AFTER))
    private void addText (MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo info, @Local(ordinal=0) Particle particle) {
        if (particle instanceof TextParticle textParticle) {
            Vec3d cameraPos = camera.getPos();
            Vec3d particlePos = textParticle.getPos();
            Vec3d prevParticlePos = textParticle.getPrevPos();
            float dX = (float)(MathHelper.lerp((double)tickDelta, prevParticlePos.x, particlePos.x) - cameraPos.getX());
            float dY = (float)(MathHelper.lerp((double)tickDelta, prevParticlePos.y, particlePos.y) - cameraPos.getY());
            float dZ = (float)(MathHelper.lerp((double)tickDelta, prevParticlePos.z, particlePos.z) - cameraPos.getZ());

            matrices.push();
            matrices.translate(dX, dY, dZ);
            matrices.multiply(camera.getRotation());

            float scaleSize = textParticle.getSize(tickDelta) / 6f;
            matrices.scale(-scaleSize, -scaleSize, -scaleSize);

            MinecraftClient.getInstance().textRenderer.draw(textParticle.getText(), 0f, 0f, textParticle.getColour(), Options.particleTextShadow, matrices.peek().getPositionMatrix(), vertexConsumers, false, 0, textParticle.getBrightness(tickDelta));
            matrices.pop();
        }
    }
}
