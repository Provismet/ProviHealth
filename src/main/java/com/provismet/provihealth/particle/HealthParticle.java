package com.provismet.provihealth.particle;

import com.provismet.lilylib.util.MoreMath;
import com.provismet.provihealth.config.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class HealthParticle extends BillboardParticle {
    private final String text;
    private final float rotationSpeed;
    private final float maxScale;
    private final int textColour;

    private float prevScale;

    protected HealthParticle (ClientWorld clientWorld, double x, double y, double z, HealthParticleEffect particleEffect, SpriteProvider provider) {
        super(clientWorld, x, y, z, provider.getFirst());

        this.red = ColorHelper.getRed(particleEffect.colour()) / 255f;
        this.green = ColorHelper.getGreen(particleEffect.colour()) / 255f;
        this.blue = ColorHelper.getBlue(particleEffect.colour()) / 255f;
        this.scale = 0f;
        this.prevScale = 0f;
        this.alpha = ColorHelper.getAlpha(particleEffect.colour()) / 255f;
        this.textColour = particleEffect.textColour();
        this.text = particleEffect.text();
        this.maxAge = 40;

        this.rotationSpeed = (float)Math.toRadians((this.random.nextDouble() * 1.5 + 0.5) * (this.random.nextBoolean() ? 10 : -10));
        this.maxScale = particleEffect.scale();

        final double sign = this.random.nextBoolean() ? 1 : -1;
        final MoreMath.RightAngledTriangle triangle = new MoreMath.RightAngledTriangle(new Vec3d(this.x, this.y, this.z), MinecraftClient.getInstance().player.getEyePos());

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

        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;

        this.world.addParticleClient(
            TextParticleEffect.fromHealthParticleEffect(particleEffect),
            true,
            true,
            this.x, this.y, this.z,
            this.velocityX, this.velocityY, this.velocityZ
        );
    }

    @Override
    public void tick () {
        super.tick();
        this.prevScale = this.scale;

        if (this.age > this.maxAge / 2) this.scale -= this.maxScale / (this.maxAge / 2f);
        else if (this.scale < this.maxScale) this.scale += this.maxScale / 5f;

        this.lastZRotation = this.zRotation;
        this.zRotation += this.rotationSpeed;

        if (Options.particleType == Options.DamageParticleType.GRAVITY) {
            if (this.onGround) {
                this.velocityX = 0;
                this.velocityY = 0;
                this.velocityZ = 0;
            }
            else this.velocityY -= 0.025;
        }
    }

    @Override
    protected RenderType getRenderType () {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
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
        return new Vec3d(this.lastX, this.lastY, this.lastZ);
    }

    public static class Factory implements ParticleFactory<HealthParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory (SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle (HealthParticleEffect particleEffect, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ, Random random) {
            HealthParticle textParticle = new HealthParticle(clientWorld, x, y, z, particleEffect, this.spriteProvider);
            textParticle.updateSprite(this.spriteProvider);
            return textParticle;
        }

    }
}
