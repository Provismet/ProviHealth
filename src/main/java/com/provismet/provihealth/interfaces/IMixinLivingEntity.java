package com.provismet.provihealth.interfaces;

import com.provismet.provihealth.util.HealthContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

public interface IMixinLivingEntity {
    HealthContainer provi_Health$getHealthContainer ();
    HealthContainer provi_Health$getMountHealthContainer ();
    List<RegistryEntry<StatusEffect>> provi_Health$getClientSideStatusEffects ();
    int provi_Health$getAnger ();
}
