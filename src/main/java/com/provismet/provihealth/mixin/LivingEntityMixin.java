package com.provismet.provihealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.particle.TextParticleEffect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IMixinLivingEntity {
    protected LivingEntityMixin (EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Unique
    private float currentHealthPercent = 1f;

    @Unique
    private float currentVehicleHealthPercent = 1f;

    @Unique
    private int prevHealth = (int)this.getHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Override
    public float provihealth_glideHealth (float glideFactor) {
        float trueValue = MathHelper.clamp(this.getHealth() / this.getMaxHealth(), 0f, 1f);
        glideFactor = MathHelper.clamp(glideFactor, 0.001f, 1f);

        this.currentHealthPercent += (trueValue - this.currentHealthPercent) * glideFactor;
        return this.currentHealthPercent;
    }

    @Override
    public float provihealth_glideVehicle (float trueValue, float glideFactor) {
        glideFactor = MathHelper.clamp(glideFactor, 0f, 1f);

        this.currentVehicleHealthPercent += (trueValue - this.currentVehicleHealthPercent) * glideFactor;
        return this.currentVehicleHealthPercent;
    }

    @Inject(method="tick", at=@At("TAIL"))
    private void spawnParticles (CallbackInfo info) {
        if ((LivingEntity)(Object)this != MinecraftClient.getInstance().getCameraEntity()) {
            if ((int)this.getHealth() < this.prevHealth && Options.spawnDamageParticles) {
                this.getWorld().addParticle(new TextParticleEffect(Options.unpackedDamage, 1f, 0.25f, String.format("%d", this.prevHealth - (int)this.getHealth())), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
            else if ((int)this.getHealth() > this.prevHealth && Options.spawnHealingParticles) {
                this.getWorld().addParticle(new TextParticleEffect(Options.unpackedHealing, 1f, 0.25f, String.format("%d", (int)this.getHealth() - this.prevHealth)), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
        }
        this.prevHealth = (int)this.getHealth();
    }
}
