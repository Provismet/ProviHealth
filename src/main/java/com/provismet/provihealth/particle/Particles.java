package com.provismet.provihealth.particle;

import com.provismet.provihealth.ProviHealthClient;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class Particles {
    public static final ParticleType<TextParticleEffect> TEXT_PARTICLE = register("text_particle", FabricParticleTypes.complex(TextParticleEffect.CODEC, TextParticleEffect.PACKET_CODEC), TextParticle.Factory::new);
    public static final ParticleType<HealthParticleEffect> HEALTH_PARTICLE = register("health_particle", FabricParticleTypes.complex(HealthParticleEffect.CODEC, HealthParticleEffect.PACKET_CODEC), HealthParticle.Factory::new);

    public static void init () {}

    private static <T extends ParticleOptions> ParticleType<T> register (String name, ParticleType<T> particle, ParticleFactoryRegistry.PendingParticleFactory<T> factoryConstructor) {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, ProviHealthClient.identifier(name), particle);
        ParticleFactoryRegistry.getInstance().register(particle, factoryConstructor);
        return particle;
    }
}
