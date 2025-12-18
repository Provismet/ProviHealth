package com.provismet.provihealth.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;

public record HealthParticleEffect (int colour, float scale, int textColour, String text) implements ParticleEffect {
    private final static Codec<String> TEXT_CODEC = Codec.string(1, 8).validate(text -> {
        try {
            Integer.valueOf(text);
            return DataResult.success(text);
        } catch (Exception e) {
            return DataResult.error(() -> "Text must be an integer: " + text);
        }
    });

    public static final MapCodec<HealthParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
                Codecs.ARGB.fieldOf("colour").forGetter(effect -> effect.colour),
                Codecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(effect -> effect.scale),
                Codecs.rangedInt(0, 0xFFFFFF).fieldOf("text_colour").forGetter(effect -> effect.textColour),
                TEXT_CODEC.fieldOf("text").forGetter(effect -> effect.text))
            .apply(instance, HealthParticleEffect::new)
    );

    public static final PacketCodec<RegistryByteBuf, HealthParticleEffect> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        effect -> effect.colour,
        PacketCodecs.FLOAT,
        effect -> effect.scale,
        PacketCodecs.INTEGER,
        effect -> effect.textColour,
        PacketCodecs.string(8),
        effect -> effect.text,
        HealthParticleEffect::new
    );

    @Override
    public ParticleType<HealthParticleEffect> getType () {
        return Particles.HEALTH_PARTICLE;
    }
}
