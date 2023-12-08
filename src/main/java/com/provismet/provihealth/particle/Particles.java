package com.provismet.provihealth.particle;

import com.provismet.provihealth.ProviHealthClient;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Particles {
    public static final ParticleType<TextParticleEffect> TEXT_PARTICLE = FabricParticleTypes.complex(TextParticleEffect.PARAMETERS_FACTORY);

    public static void register () {
        Registry.register(Registries.PARTICLE_TYPE, ProviHealthClient.identifier("text_particle"), TEXT_PARTICLE);
        ParticleFactoryRegistry.getInstance().register(TEXT_PARTICLE, TextParticle.Factory::new);
    }
}
