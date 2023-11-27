package com.provismet.provihealth.hud;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import com.provismet.provihealth.ProviHealthClient;

import net.minecraft.entity.EntityGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BorderRegistry {
    private static final HashMap<EntityGroup, Identifier> groupBorders = new HashMap<>();
    private static final HashMap<EntityGroup, ItemStack> groupItems = new HashMap<>();

    private static final Identifier DEFAULT = ProviHealthClient.identifier("textures/gui/healthbars/default.png");

    public static void registerEntityGroupBorder (EntityGroup group, Identifier border) {
        if (group == null || border == null) ProviHealthClient.LOGGER.error("Attempted to register a null object to the border registry.");
        else if (groupBorders.containsKey(group)) ProviHealthClient.LOGGER.error("Attempted to register an already registered entity group.");
        else groupBorders.put(group, border);
    }

    public static void registerItem (EntityGroup group, ItemStack item) {
        if (group == null || item == null) ProviHealthClient.LOGGER.error("Attempted to register a null object to the item registry.");
        else if (groupItems.containsKey(group)) ProviHealthClient.LOGGER.error("Attempted to register an already registered entity group.");
        else groupItems.put(group, item);
    }

    public static void registerItem (EntityGroup group, Item item) {
        registerItem(group, item.getDefaultStack());
    }

    public static Identifier getBorder (@Nullable EntityGroup group) {
        if (group == null || !groupBorders.containsKey(group)) return DEFAULT;
        else return groupBorders.get(group);
    }

    @Nullable
    public static ItemStack getItem (EntityGroup group) {
        if (group == null || !groupItems.containsKey(group)) return null;
        else return groupItems.get(group);
    }
}
