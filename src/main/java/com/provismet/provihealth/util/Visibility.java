package com.provismet.provihealth.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Visibility {
    @SuppressWarnings("resource")
    public static boolean isVisible (LivingEntity living) {
        if (!living.isInvisibleTo(MinecraftClient.getInstance().player)) return true;
        if (living instanceof PlayerEntity player && player.isSpectator()) return false;
        if (living.hasPassengers()) return true;
        if (!living.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return true;
        if (!living.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) return true;
        if (living.isGlowing()) return true;
        return false;
    }
}
