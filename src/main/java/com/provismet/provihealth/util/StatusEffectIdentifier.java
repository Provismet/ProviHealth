package com.provismet.provihealth.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ColorHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class StatusEffectIdentifier {
    private static final Map<Integer, RegistryEntry<StatusEffect>> colourToEffect = new HashMap<>();

    public static RegistryEntry<StatusEffect> fromParticleEffect (EntityEffectParticleEffect particleEffect) {
        return colourToEffect.getOrDefault(particleEffect.color, null);
    }

    public static void setup () {
        Registries.STATUS_EFFECT.streamEntries().forEach(effect -> colourToEffect.putIfAbsent(ColorHelper.fullAlpha(effect.value().getColor()), effect));
    }
}
