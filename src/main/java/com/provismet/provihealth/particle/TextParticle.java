package com.provismet.provihealth.particle;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.DamageParticleType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class TextParticle extends Particle {
    private final String text;
    private final float maxScale;
    private final int textColour;

    private float scale;
    private float prevScale;

    protected TextParticle (ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ, TextParticleEffect particleEffect) {
        super(clientWorld, x, y, z);

        this.scale = 0f;
        this.prevScale = 0f;
        this.textColour = particleEffect.textColour();
        this.text = particleEffect.text();
        this.maxAge = 40;
        this.maxScale = particleEffect.scale();

        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;

        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        if (Options.particleType == DamageParticleType.RISING) this.velocityMultiplier = 0.85f;
    }

    @Override
	public void tick () {
        super.tick();
        this.prevScale = this.scale;

        if (this.age > this.maxAge / 2) this.scale -= this.maxScale / (this.maxAge / 2f);
        else if (this.scale < this.maxScale) this.scale += this.maxScale / 5f;

        if (Options.particleType == DamageParticleType.GRAVITY) {
            if (this.onGround) {
                this.velocityX = 0;
                this.velocityY = 0;
                this.velocityZ = 0;
            }
            else this.velocityY -= 0.025;
        }
	}

//    @Override
//    public void renderCustom (MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, float tickDelta) {
//        super.renderCustom(matrices, vertexConsumers, camera, tickDelta);
//
//        matrices.push();
//        float dX = (float)(MathHelper.lerp(tickDelta, this.lastX, this.x) - camera.getPos().getX());
//        float dY = (float)(MathHelper.lerp(tickDelta, this.lastY, this.y) - camera.getPos().getY());
//        float dZ = (float)(MathHelper.lerp(tickDelta, this.lastZ, this.z) - camera.getPos().getZ());
//
//        matrices.translate(dX, dY, dZ);
//        matrices.multiply(camera.getRotation());
//        float scaleSize = this.getSize(tickDelta) / 6f;
//        matrices.scale(scaleSize, -scaleSize, scaleSize);
//
//        this.textRenderer.draw(
//            this.text,
//            0f, 0f,
//            this.textColour,
//            Options.particleTextShadow,
//            matrices.peek().getPositionMatrix(),
//            vertexConsumers,
//            TextRenderer.TextLayerType.POLYGON_OFFSET,
//            0,
//            this.getBrightness(tickDelta)
//        );
//        matrices.pop();
//    }

    @Override
    public ParticleTextureSheet textureSheet () {
        return TextParticleRenderer.PARTICLE_TEXTURE_SHEET;
    }

    @Override
    public int getBrightness (float tint) {
        return LightmapTextureManager.MAX_LIGHT_COORDINATE;
    }

    public String getText () {
        return this.text;
    }

    public int getColour () {
        return this.textColour;
    }

    public float getSize (float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevScale, this.scale);
    }

    public Vec3d getPos () {
        return new Vec3d(this.x, this.y, this.z);
    }

    public Vec3d getPrevPos () {
        return new Vec3d(this.lastX, this.lastY, this.lastZ);
    }

    public static class Factory implements ParticleFactory<TextParticleEffect> {
        public Factory (SpriteProvider spriteProvider) {}

        @Override
        public Particle createParticle (TextParticleEffect particleEffect, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ, Random random) {
            return new TextParticle(clientWorld, x, y, z, velX, velY, velZ, particleEffect);
        }

    }
}
