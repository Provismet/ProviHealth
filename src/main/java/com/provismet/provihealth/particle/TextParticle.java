package com.provismet.provihealth.particle;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.DamageParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class TextParticle extends Particle {
    private final String text;
    private final float maxScale;
    private final int textColour;

    private float scale;
    private float prevScale;

    protected TextParticle (ClientLevel clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ, TextParticleEffect particleEffect) {
        super(clientWorld, x, y, z);

        this.scale = 0f;
        this.prevScale = 0f;
        this.textColour = particleEffect.textColour();
        this.text = particleEffect.text();
        this.lifetime = 40;
        this.maxScale = particleEffect.scale();

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        if (Options.particleType == DamageParticleType.RISING) this.friction = 0.85f;
    }

    @Override
	public void tick () {
        super.tick();
        this.prevScale = this.scale;

        if (this.age > this.lifetime / 2) this.scale -= this.maxScale / (this.lifetime / 2f);
        else if (this.scale < this.maxScale) this.scale += this.maxScale / 5f;

        if (Options.particleType == DamageParticleType.GRAVITY) {
            if (this.onGround) {
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;
            }
            else this.yd -= 0.025;
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
    public ParticleRenderType getGroup () {
        return TextParticleRenderer.PARTICLE_TEXTURE_SHEET;
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

    public float getSize (float tickDelta) {
        return Mth.lerp(tickDelta, this.prevScale, this.scale);
    }

    public Vec3 getPos () {
        return new Vec3(this.x, this.y, this.z);
    }

    public Vec3 getPrevPos () {
        return new Vec3(this.xo, this.yo, this.zo);
    }

    public static class Factory implements ParticleProvider<TextParticleEffect> {
        public Factory (SpriteSet spriteProvider) {}

        @Override
        public Particle createParticle (TextParticleEffect particleEffect, ClientLevel clientWorld, double x, double y, double z, double velX, double velY, double velZ, RandomSource random) {
            return new TextParticle(clientWorld, x, y, z, velX, velY, velZ, particleEffect);
        }

    }
}
