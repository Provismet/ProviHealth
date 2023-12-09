package com.provismet.provihealth.util;

import com.provismet.provihealth.config.Options;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.DisplayEntity;

public class Visibility {
    @SuppressWarnings("resource")
    public static boolean isVisible (LivingEntity living) {
        if (!living.isInvisibleTo(MinecraftClient.getInstance().player)) return true;
        if (living.hasPassengers()) return true;
        if (!living.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return true;
        if (!living.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) return true;
        if (living.isGlowing()) return true;
        if (living.getWorld().getEntitiesByClass(DisplayEntity.class, living.getBoundingBox().expand(Options.boxSize), x -> true).size() > 0) return true;
        return true;
    }
}
