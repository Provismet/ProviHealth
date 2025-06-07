package com.provismet.provihealth.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record TextParticleEffect (float scale, int textColour, String text) implements ParticleEffect {
    private final static Codec<String> TEXT_CODEC = Codec.string(1, 8).validate(text -> {
        try {
            // Integer.valueOf(text);
            // String.valueOf(text);
            return DataResult.success(text);
        } catch (Exception e) {
            return DataResult.error(() -> "Text must be an integer: " + text);
        }
    });

    public static final MapCodec<TextParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
                Codecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(effect -> effect.scale),
                Codecs.rangedInt(0, 0xFFFFFF).fieldOf("text_colour").forGetter(effect -> effect.textColour),
                TEXT_CODEC.fieldOf("text").forGetter(effect -> effect.text))
            .apply(instance, TextParticleEffect::new)
    );

    public static final PacketCodec<RegistryByteBuf, TextParticleEffect> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT,
        effect -> effect.scale,
        PacketCodecs.INTEGER,
        effect -> effect.textColour,
        PacketCodecs.string(8),
        effect -> effect.text,
        TextParticleEffect::new
    );

    public static TextParticleEffect fromHealthParticleEffect (HealthParticleEffect effect) {
        return new TextParticleEffect(effect.scale(), effect.textColour(), effect.text());
    }

    @Override
    public ParticleType<TextParticleEffect> getType () {
        return Particles.TEXT_PARTICLE;
    }
}
