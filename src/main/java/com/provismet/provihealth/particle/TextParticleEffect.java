package com.provismet.provihealth.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record TextParticleEffect (float scale, int textColour, String text) implements ParticleOptions {
    private final static Codec<String> TEXT_CODEC = Codec.string(1, 8).validate(text -> {
        try {
            Integer.valueOf(text);
            return DataResult.success(text);
        } catch (Exception e) {
            return DataResult.error(() -> "Text must be an integer: " + text);
        }
    });

    public static final MapCodec<TextParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
                ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(effect -> effect.scale),
                ExtraCodecs.intRange(0, 0xFFFFFF).fieldOf("text_colour").forGetter(effect -> effect.textColour),
                TEXT_CODEC.fieldOf("text").forGetter(effect -> effect.text))
            .apply(instance, TextParticleEffect::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TextParticleEffect> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        effect -> effect.scale,
        ByteBufCodecs.INT,
        effect -> effect.textColour,
        ByteBufCodecs.stringUtf8(8),
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
