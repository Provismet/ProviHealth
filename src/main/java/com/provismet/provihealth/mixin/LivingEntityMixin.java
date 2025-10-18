package com.provismet.provihealth.mixin;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.particle.HealthParticleEffect;
import com.provismet.provihealth.util.HealthCalculator;
import com.provismet.provihealth.util.HealthContainer;
import com.provismet.provihealth.util.StatusEffectIdentifier;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.EffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IMixinLivingEntity {
    protected LivingEntityMixin (EntityType<?> type, World world) {
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

    @Shadow @Final private static TrackedData<List<ParticleEffect>> POTION_SWIRLS;

    @Override
    public HealthContainer provi_Health$getHealthContainer () {
        return this.container;
    }

    @Override
    public HealthContainer provi_Health$getMountHealthContainer () {
        return this.mountContainer;
    }

    @Override
    public List<RegistryEntry<StatusEffect>> provi_Health$getClientSideStatusEffects () {
        List<ParticleEffect> particles = this.dataTracker.get(POTION_SWIRLS);
        if (particles.isEmpty()) return List.of();

        return particles.stream()
            .filter(particle -> particle instanceof EffectParticleEffect)
            .map(particle -> StatusEffectIdentifier.fromParticleEffect((EffectParticleEffect)particle))
            .distinct()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(effect -> effect.value().getName().getString()))
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

        final Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity != null && this != cameraEntity && this.distanceTo(MinecraftClient.getInstance().getCameraEntity()) <= Options.maxParticleDistance) {
            if (this.container.getCurrent() < this.container.getPrevious() && Options.spawnDamageParticles) {
                this.getEntityWorld().addParticleClient(new HealthParticleEffect(Options.unpackedDamage, Options.damageAlpha, Options.particleScale, Options.damageParticleTextColour, String.format("%d", (int)this.container.getPrevious() - (int)this.container.getCurrent())), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
            else if (this.container.getCurrent() > this.container.getPrevious() && Options.spawnHealingParticles) {
                this.getEntityWorld().addParticleClient(new HealthParticleEffect(Options.unpackedHealing, Options.healingAlpha, Options.particleScale, Options.healingParticleTextColour, String.format("%d", (int)this.container.getCurrent() - (int)this.container.getPrevious())), this.getX(), this.getEyeY(), this.getZ(), 0f, 0f, 0f);
            }
        }
    }
}
