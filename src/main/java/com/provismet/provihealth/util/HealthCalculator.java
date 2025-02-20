package com.provismet.provihealth.util;

import com.provismet.provihealth.config.Options;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface HealthCalculator {
    @Nullable
    static HealthContainer getRecursiveMountHealth (Entity rider, @Nullable Options.BarType barType) {
        if (!rider.hasVehicle()) return null;

        float vehicleHealthDeep = 0f;
        float vehicleMaxHealthDeep = 0f;

        Entity currentMount = rider.getVehicle();
        while (currentMount != null) {
            if (currentMount instanceof LivingEntity currentLiving && !Options.isBlacklisted(currentMount, barType)) {
                vehicleHealthDeep += currentLiving.getHealth();
                vehicleMaxHealthDeep += currentLiving.getMaxHealth();
            }
            currentMount = currentMount.getVehicle();
        }

        if (vehicleMaxHealthDeep == 0f) return null;
        return new HealthContainer(vehicleHealthDeep, vehicleMaxHealthDeep);
    }
}
