package com.provismet.provihealth.datagen;

import com.provismet.lilylib.datagen.provider.LilyParticleTextureProvider;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.particle.Particles;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class ParticleGenerator extends LilyParticleTextureProvider {
    protected ParticleGenerator (FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    protected void generate (HolderLookup.Provider wrapperLookup, ParticleWriter particleWriter) {
        particleWriter.add(Particles.HEALTH_PARTICLE, ProviHealthClient.identifier("hit_marker"));
        particleWriter.add(Particles.TEXT_PARTICLE, ProviHealthClient.identifier("hit_marker")); // The texture is unused for this particle.
    }
}
