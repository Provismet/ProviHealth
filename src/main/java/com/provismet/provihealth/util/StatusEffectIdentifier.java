package com.provismet.provihealth.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffect;

public abstract class StatusEffectIdentifier {
    private static final Map<Integer, Holder<MobEffect>> colourToEffect = new HashMap<>();

    @Nullable
    public static Holder<MobEffect> fromParticleEffect (ColorParticleOption particleEffect) {
        return colourToEffect.getOrDefault(particleEffect.color, null);
    }

    public static void setup () {
        BuiltInRegistries.MOB_EFFECT.listElements().forEach(effect -> colourToEffect.putIfAbsent(ARGB.opaque(effect.value().getColor()), effect));
    }
}
