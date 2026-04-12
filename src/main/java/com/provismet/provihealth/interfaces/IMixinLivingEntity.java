package com.provismet.provihealth.interfaces;

import com.provismet.provihealth.util.HealthContainer;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public interface IMixinLivingEntity {
    HealthContainer provi_Health$getHealthContainer ();
    HealthContainer provi_Health$getMountHealthContainer ();
    List<Holder<MobEffect>> provi_Health$getClientSideStatusEffects ();
}
