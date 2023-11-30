package com.provismet.provihealth.hud;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BorderRegistry {
    private static final HashMap<EntityGroup, Identifier> groupBorders = new HashMap<>();
    private static final HashMap<EntityGroup, ItemStack> groupItems = new HashMap<>();
    private static final HashMap<EntityType<?>, Identifier> overrideBorders = new HashMap<>();
    private static final HashMap<EntityType<?>, ItemStack> overrideItems = new HashMap<>();

    private static final Identifier DEFAULT = ProviHealthClient.identifier("textures/gui/healthbars/default.png");

    public static void registerBorder (EntityGroup group, Identifier border) {
        if (group == null || border == null) ProviHealthClient.LOGGER.error("Attempted to register a null object to the border registry.");
        else if (groupBorders.containsKey(group)) ProviHealthClient.LOGGER.error("Attempted to register an already registered entity group.");
        else groupBorders.put(group, border);
    }

    public static void registerItem (EntityGroup group, ItemStack item) {
        if (group == null || item == null) ProviHealthClient.LOGGER.error("Attempted to register a null object to the icon registry.");
        else if (groupItems.containsKey(group)) ProviHealthClient.LOGGER.error("Attempted to register an already registered entity group.");
        else groupItems.put(group, item);
    }

    public static boolean registerBorder (EntityType<?> type, Identifier border, boolean force) {
        if (type == null || border == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null object to the border registry.");
            return false;
        }
        else {
            if (overrideBorders.containsKey(type)) {
                if (force) {
                    ProviHealthClient.LOGGER.warn("The border for " + type.getName().getString() + " has been overridden.");
                }
                else return false;
            }
            overrideBorders.put(type, border);
            return true;
        }
    }

    public static boolean registerItem (EntityType<?> type, ItemStack item, boolean force) {
        if (type == null || item == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null object to the icon registry.");
            return false;
        }
        else {
            if (overrideItems.containsKey(type)) {
                if (force) {
                    ProviHealthClient.LOGGER.warn("The icon for " + type.getName().getString() + " has been overridden.");
                }
                else return false;
            }
            overrideItems.put(type, item);
            return true;
        }
    }

    public static void registerItem (EntityGroup group, Item item) {
        registerItem(group, item.getDefaultStack());
    }

    public static Identifier getBorder (@Nullable LivingEntity entity) {
        if (entity == null || !Options.useCustomHudPortraits) return DEFAULT;
        else if (overrideBorders.containsKey(entity.getType())) return overrideBorders.get(entity.getType());
        else if (groupBorders.containsKey(entity.getGroup())) return groupBorders.get(entity.getGroup());
        else return DEFAULT;
    }

    @Nullable
    public static ItemStack getItem (LivingEntity entity) {
        if (entity == null) return null;
        else if (overrideItems.containsKey(entity.getType())) return overrideItems.get(entity.getType());
        else if (groupItems.containsKey(entity.getGroup())) return groupItems.get(entity.getGroup());
        else return null;
    }
}
