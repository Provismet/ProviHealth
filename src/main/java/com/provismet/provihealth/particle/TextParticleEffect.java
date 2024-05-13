package com.provismet.provihealth.particle;

import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class TextParticleEffect implements ParticleEffect {
    private final Vec3f colour;

    public final float alpha;
    public final float scale;
    public final String text;
    public final int textColour;

    public TextParticleEffect (Vec3f colour, float alpha, float scale, int textColour, String text) {
        this.colour = colour;
        this.alpha = alpha;
        this.scale = scale;
        this.text = text;
        this.textColour = textColour;
    }

    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<TextParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<TextParticleEffect>() {
        @Override
        public TextParticleEffect read (ParticleType<TextParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            Vec3f colour = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float alpha = stringReader.readFloat();
            stringReader.expect(' ');
            float scale = stringReader.readFloat();
            stringReader.expect(' ');
            int textColour = stringReader.readInt();
            stringReader.expect(' ');
            String text = stringReader.getRemaining();
            return new TextParticleEffect(colour, alpha, scale, textColour, text);
        }

        @Override
        public TextParticleEffect read (ParticleType<TextParticleEffect> type, PacketByteBuf buffer) {
            return new TextParticleEffect(AbstractDustParticleEffect.readColor(buffer), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readString());
        }
    };

    @Override
    public String asString () {
        return String.format("%s %.2f %.2f %.2f %.2f %.2f %d %s",
                Registry.PARTICLE_TYPE.getId(this.getType()),
                this.colour.getX(),
                this.colour.getY(),
                this.colour.getZ(),
                this.alpha,
                this.scale,
                this.textColour,
                this.text
        );
    }

    @Override
    public ParticleType<TextParticleEffect> getType() {
        return Particles.TEXT_PARTICLE;
    }

    @Override
    public void write (PacketByteBuf buffer) {
        buffer.writeFloat(this.colour.getX());
        buffer.writeFloat(this.colour.getY());
        buffer.writeFloat(this.colour.getZ());
        buffer.writeFloat(this.alpha);
        buffer.writeFloat(this.scale);
        buffer.writeInt(this.textColour);
        buffer.writeString(this.text);
    }

    public Vec3f getColour () {
        return this.colour;
    }
}
