package com.provismet.provihealth.mixin;

import com.provismet.provihealth.particle.TextParticleRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "createParticleRenderer", at = @At("HEAD"), cancellable = true)
    private void addCustomRenderer (ParticleTextureSheet textureSheet, CallbackInfoReturnable<ParticleRenderer<?>> cir) {
        if (textureSheet == TextParticleRenderer.PARTICLE_TEXTURE_SHEET) {
            cir.setReturnValue(new TextParticleRenderer((ParticleManager)(Object)this));
        }
    }
}
