package com.provismet.provihealth.interfaces;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

public interface IMixinLivingEntity {
    public float provihealth_glideHealth (float glideFactor);

    public float provihealth_glideVehicle (float trueValue, float glideFactor);

    List<RegistryEntry<StatusEffect>> provi_Health$getClientSideStatusEffects ();
}
