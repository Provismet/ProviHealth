package com.provismet.provihealth.mixin;

import com.provismet.provihealth.particle.TextParticleRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticlesRenderState;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {
    @Shadow @Final
    private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void addCustomRenderer (ParticleRenderType textureSheet, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (textureSheet == TextParticleRenderer.PARTICLE_TEXTURE_SHEET) {
            cir.setReturnValue(new TextParticleRenderer((ParticleEngine)(Object)this));
        }
    }

    @Inject(method = "extract", at = @At("HEAD"))
    private void addCustom (ParticlesRenderState batch, Frustum frustum, Camera camera, float tickProgress, CallbackInfo info) {
        ParticleGroup<?> particleRenderer = this.particles.get(TextParticleRenderer.PARTICLE_TEXTURE_SHEET);
        if (particleRenderer != null && !particleRenderer.isEmpty()) {
            batch.add(particleRenderer.extractRenderState(frustum, camera, tickProgress));
        }
    }
}
