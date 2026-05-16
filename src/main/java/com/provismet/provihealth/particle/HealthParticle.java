package com.provismet.provihealth.particle;

import com.provismet.lilylib.util.MoreMath;
import com.provismet.provihealth.config.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class HealthParticle extends SingleQuadParticle {
    private final String text;
    private final float rotationSpeed;
    private final float maxScale;
    private final int textColour;

    private float prevScale;

    protected HealthParticle (ClientLevel clientWorld, double x, double y, double z, HealthParticleEffect particleEffect, SpriteSet provider) {
        super(clientWorld, x, y, z, provider.first());

        this.rCol = ARGB.red(particleEffect.colour()) / 255f;
        this.gCol = ARGB.green(particleEffect.colour()) / 255f;
        this.bCol = ARGB.blue(particleEffect.colour()) / 255f;
        this.quadSize = 0f;
        this.prevScale = 0f;
        this.alpha = ARGB.alpha(particleEffect.colour()) / 255f;
        this.textColour = particleEffect.textColour();
        this.text = particleEffect.text();
        this.lifetime = 40;

        this.rotationSpeed = (float)Math.toRadians((this.random.nextDouble() * 1.5 + 0.5) * (this.random.nextBoolean() ? 10 : -10));
        this.maxScale = particleEffect.scale();

        final double sign = this.random.nextBoolean() ? 1 : -1;
        final MoreMath.RightAngledTriangle triangle = new MoreMath.RightAngledTriangle(new Vec3(this.x, this.y, this.z), Minecraft.getInstance().player.getEyePosition());

        switch (Options.particleType) {
            case RISING:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y, this.z + 0.5 * triangle.sine() * sign);
                this.xd = 0;
                this.yd = 0.1;
                this.zd = 0;
                this.friction = 0.85f;
                break;

            case GRAVITY:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y + this.random.nextDouble() * 0.5, this.z + 0.5 * triangle.sine() * sign);
                double velBonus = this.random.nextDouble() * 0.025 + 0.05;
                this.xd = velBonus * -triangle.cosine() * sign;
                this.yd = 0.125;
                this.zd = velBonus * triangle.sine() * sign;
                break;

            case STATIC:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y + this.random.nextDouble() * 0.75, this.z + 0.5 * triangle.sine() * sign);
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;
                break;

            default:
                break;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.level.addParticle(
            TextParticleEffect.fromHealthParticleEffect(particleEffect),
            true,
            true,
            this.x, this.y, this.z,
            this.xd, this.yd, this.zd
        );
    }

    @Override
    public void tick () {
        super.tick();
        this.prevScale = this.quadSize;

        if (this.age > this.lifetime / 2) this.quadSize -= this.maxScale / (this.lifetime / 2f);
        else if (this.quadSize < this.maxScale) this.quadSize += this.maxScale / 5f;

        this.oRoll = this.roll;
        this.roll += this.rotationSpeed;

        if (Options.particleType == Options.DamageParticleType.GRAVITY) {
            if (this.onGround) {
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;
            }
            else this.yd -= 0.025;
        }
    }

    @Override
    protected Layer getLayer () {
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords (float tint) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public String getText () {
        return this.text;
    }

    public int getColour () {
        return this.textColour;
    }

    @Override
    public float getQuadSize (float tickDelta) {
        return Mth.lerp(tickDelta, this.prevScale, this.quadSize);
    }

    public Vec3 getPos () {
        return new Vec3(this.x, this.y, this.z);
    }

    public Vec3 getPrevPos () {
        return new Vec3(this.xo, this.yo, this.zo);
    }

    public static class Factory implements ParticleProvider<HealthParticleEffect> {
        private final SpriteSet spriteProvider;

        public Factory (SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle (HealthParticleEffect particleEffect, ClientLevel clientWorld, double x, double y, double z, double velX, double velY, double velZ, RandomSource random) {
            HealthParticle textParticle = new HealthParticle(clientWorld, x, y, z, particleEffect, this.spriteProvider);
            textParticle.setSpriteFromAge(this.spriteProvider);
            return textParticle;
        }

    }
}
