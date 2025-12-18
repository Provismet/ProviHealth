package com.provismet.provihealth.mixin;

import com.provismet.provihealth.particle.TextParticleRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Shadow @Final
    private Map<ParticleTextureSheet, ParticleRenderer<?>> particles;

    @Inject(method = "createParticleRenderer", at = @At("HEAD"), cancellable = true)
    private void addCustomRenderer (ParticleTextureSheet textureSheet, CallbackInfoReturnable<ParticleRenderer<?>> cir) {
        if (textureSheet == TextParticleRenderer.PARTICLE_TEXTURE_SHEET) {
            cir.setReturnValue(new TextParticleRenderer((ParticleManager)(Object)this));
        }
    }

    @Inject(method = "addToBatch", at = @At("HEAD"))
    private void addCustom (SubmittableBatch batch, Frustum frustum, Camera camera, float tickProgress, CallbackInfo info) {
        ParticleRenderer<?> particleRenderer = this.particles.get(TextParticleRenderer.PARTICLE_TEXTURE_SHEET);
        if (particleRenderer != null && !particleRenderer.isEmpty()) {
            batch.add(particleRenderer.render(frustum, camera, tickProgress));
        }
    }
}
