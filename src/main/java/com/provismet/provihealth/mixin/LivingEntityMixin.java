package com.provismet.provihealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.provismet.provihealth.interfaces.IMixinLivingEntity;

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
}
