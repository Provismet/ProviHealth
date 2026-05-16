package com.provismet.provihealth.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class Visibility {
    public static boolean isVisible (LivingEntity living) {
        if (Minecraft.getInstance().player != null && !living.isInvisibleTo(Minecraft.getInstance().player)) return true;
        if (living instanceof Player player && player.isSpectator()) return false;
        if (living.isVehicle()) return true;
        if (!living.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return true;
        if (!living.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) return true;
        return living.isCurrentlyGlowing();
    }
}
