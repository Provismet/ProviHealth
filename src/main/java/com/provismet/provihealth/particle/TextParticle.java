package com.provismet.provihealth.particle;

import com.provismet.lilylib.util.MoreMath.RightAngledTriangle;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.DamageParticleType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TextParticle extends SpriteBillboardParticle {
    private final String text;
    private final float rotationSpeed;
    private final float maxScale;
    private final int textColour;

    private float prevScale;

    @SuppressWarnings("resource")
    protected TextParticle (ClientWorld clientWorld, double x, double y, double z, TextParticleEffect particleEffect) {
        super(clientWorld, x, y, z);

        this.red = particleEffect.getColour().getX();
        this.green = particleEffect.getColour().getY();
        this.blue = particleEffect.getColour().getZ();
        this.scale = 0f;
        this.prevScale = 0f;
        this.alpha = particleEffect.alpha;
        this.textColour = particleEffect.textColour;
        this.text = particleEffect.text;
        this.maxAge = 40;

        this.rotationSpeed = (float)Math.toRadians((this.random.nextDouble() * 1.5 + 0.5) * (this.random.nextBoolean() ? 10 : -10));
        this.maxScale = particleEffect.scale;

        final double sign = this.random.nextBoolean() ? 1 : -1;
        final RightAngledTriangle triangle = new RightAngledTriangle(new Vec3d(this.x, this.y, this.z), MinecraftClient.getInstance().player.getEyePos());

        switch (Options.particleType) {
            case RISING:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y, this.z + 0.5 * triangle.sine() * sign);
                this.velocityX = 0;
                this.velocityY = 0.1;
                this.velocityZ = 0;
                this.velocityMultiplier = 0.85f;
                break;

            case GRAVITY:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y + this.random.nextDouble() * 0.5, this.z + 0.5 * triangle.sine() * sign);
                double velBonus = this.random.nextDouble() * 0.025 + 0.05;
                this.velocityX = velBonus * -triangle.cosine() * sign;
                this.velocityY = 0.125;
                this.velocityZ = velBonus * triangle.sine() * sign;
                break;

            case STATIC:
                this.setPos(this.x + 0.5 * -triangle.cosine() * sign, this.y + this.random.nextDouble() * 0.75, this.z + 0.5 * triangle.sine() * sign);
                this.velocityX = 0;
                this.velocityY = 0;
                this.velocityZ = 0;
                break;

            default:
                break;
        }

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
    }

    @Override
    public void tick () {
        super.tick();
        this.prevScale = this.scale;

        if (this.age > this.maxAge / 2) this.scale -= this.maxScale / (this.maxAge / 2f);
        else if (this.scale < this.maxScale) this.scale += this.maxScale / 5f;

        this.prevAngle = this.angle;
        this.angle += this.rotationSpeed;

        if (Options.particleType == DamageParticleType.GRAVITY) {
            if (this.onGround) {
                this.velocityX = 0;
                this.velocityY = 0;
                this.velocityZ = 0;
            }
            else this.velocityY -= 0.025;
        }
    }

    @Override
    public ParticleTextureSheet getType () {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness (float tint) {
        return LightmapTextureManager.pack(15, 15);
    }

    public String getText () {
        return this.text;
    }

    public int getColour () {
        return this.textColour;
    }

    @Override
    public float getSize (float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevScale, this.scale);
    }

    public Vec3d getPos () {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3d getPrevPos () {
        return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
    }

    public static class Factory implements ParticleFactory<TextParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory (SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle (TextParticleEffect particleEffect, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ) {
            TextParticle textParticle = new TextParticle(clientWorld, x, y, z, particleEffect);
            textParticle.setSprite(this.spriteProvider);
            return textParticle;
        }

    }
}
