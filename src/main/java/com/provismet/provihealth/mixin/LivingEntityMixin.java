package com.provismet.provihealth.mixin;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.particle.HealthParticleEffect;
import com.provismet.provihealth.util.HealthCalculator;
import com.provismet.provihealth.util.HealthContainer;
import com.provismet.provihealth.util.StatusEffectIdentifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IMixinLivingEntity {
    protected LivingEntityMixin (EntityType<?> type, Level world) {
        super(type, world);
    }

    @Unique
    private HealthContainer container;

    @Unique
    private HealthContainer mountContainer;

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract float getMaxHealth();

    @Shadow @Final private static EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;

    @Override
    public HealthContainer provi_Health$getHealthContainer () {
        return this.container;
    }

    @Override
    public HealthContainer provi_Health$getMountHealthContainer () {
        return this.mountContainer;
    }

    @Override
    public List<Holder<MobEffect>> provi_Health$getClientSideStatusEffects () {
        List<ParticleOptions> particles = this.entityData.get(DATA_EFFECT_PARTICLES);
        if (particles.isEmpty()) return List.of();

        return particles.stream()
            .filter(particle -> particle instanceof ColorParticleOption)
            .map(particle -> StatusEffectIdentifier.fromParticleEffect((ColorParticleOption)particle))
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.comparing(effect -> effect.value().getDisplayName().getString()))
            .sorted(Comparator.comparingInt(effect -> effect.value().getCategory().ordinal()))
            .toList();
    }

    @Inject(method="tick", at=@At("TAIL"))
    private void spawnParticles (CallbackInfo info) {
        if (this.container == null) this.container = new HealthContainer(this.getHealth());
        this.container.set(this.getHealth());
        this.container.setMaxHealth(this.getMaxHealth());

        HealthContainer currentMountHealth = HealthCalculator.getRecursiveMountHealth(this, Options.BarType.WORLD);
        if (this.mountContainer == null || currentMountHealth == null) this.mountContainer = currentMountHealth;
        else this.mountContainer.setFrom(currentMountHealth);

        final Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity != null && this != cameraEntity && this.distanceTo(Minecraft.getInstance().getCameraEntity()) <= Options.maxParticleDistance) {
            if (this.container.getCurrent() < this.container.getPrevious() && Options.spawnDamageParticles) {
                this.level().addParticle(new HealthParticleEffect(ARGB.color(Options.damageAlpha, Options.damageColour), Options.particleScale, Options.damageParticleTextColour, String.format("%d", (int)this.container.getPrevious() - (int)this.container.getCurrent())), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
            else if (this.container.getCurrent() > this.container.getPrevious() && Options.spawnHealingParticles) {
                this.level().addParticle(new HealthParticleEffect(ARGB.color(Options.healingAlpha, Options.healingColour), Options.particleScale, Options.healingParticleTextColour, String.format("%d", (int)this.container.getCurrent() - (int)this.container.getPrevious())), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
        }
    }
}
