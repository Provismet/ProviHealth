package com.provismet.provihealth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.provismet.provihealth.hud.BorderRegistry;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface ProviHealthApi {
    public void onInitialize ();

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * Only one icon may be registered per entity group.
     *
     * The icon takes the form of an item.
     *
     * @param entityGroup The entity group.
     * @param item The item to serve as the icon.
     * @return Whether or not the registry succeeded. This is false if the entity group already has an icon.
     */
    public default boolean registerIcon (EntityGroup entityGroup, @NotNull Item item) {
        return this.registerIcon(entityGroup, item, false);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * Only one icon may be registered per entity group.
     *
     * The icon takes the form of an item.
     *
     * @param entityGroup The entity group.
     * @param item The item to serve as the icon.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerIcon (EntityGroup entityGroup, @NotNull Item item, boolean force) {
        return BorderRegistry.registerItem(entityGroup, item.getDefaultStack(), force);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type.
     * If available, icons for entity types will suppress icons for entity groups.
     *
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity type.
     * @param item The item that will act as the icon for this entity type.
     * @return Whether or not the registry succeeded. This is false if the entity type already has an icon.
     */
    public default boolean registerIcon (EntityType<?> type, @NotNull Item item) {
        return this.registerIcon(type, item, false);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type.
     * If available, icons for entity types will suppress icons for entity groups.
     *
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity type.
     * @param item The item that will act as the icon for this entity type.
     * @param force Whether or not this registration should override any pre-existing icon for the same entity type.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerIcon (EntityType<?> type, @NotNull Item item, boolean force) {
        return BorderRegistry.registerItem(type, item.getDefaultStack(), force);
    }


    /**
     * Sets an icon to appear on the HUD for a certain entity type. This method accepts ItemStacks with NBT.
     * If available, icons for entity types will suppress icons for entity groups.
     *
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     *
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity type.
     * @param item The item stack that will act as the icon for this entity type.
     * @return Whether or not the registry succeeded. This is false if the entity type already has an icon.
     */
    public default boolean registerIconStack (EntityType<?> type, @Nullable ItemStack item) {
        return this.registerIconStack(type, item, false);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type. This method accepts ItemStacks with NBT.
     * If available, icons for entity types will suppress icons for entity groups.
     *
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     *
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity type.
     * @param item The item stack that will act as the icon for this entity type.
     * @param force Whether or not this registration should override any pre-existing icon for the same entity type.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerIconStack (EntityType<?> type, @Nullable ItemStack item, boolean force) {
        return BorderRegistry.registerItem(type, item, force);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * This method accepts an ItemStack with NBT. Only one icon may be registered per entity group.
     *
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     *
     * Only one icon can be registered per entity group. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity group.
     * @param item The item stack that will act as the icon for this entity group.
     * @return Whether or not the registry succeeded. This is false if the entity group already has an icon.
     */
    public default boolean registerIconStack (EntityGroup type, @Nullable ItemStack item) {
        return this.registerIconStack(type, item, false);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * This method accepts an ItemStack with NBT. Only one icon may be registered per entity group.
     *
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     *
     * Only one icon can be registered per entity group. This registry may suppress or be suppressed by other mods.
     *
     * @param type The entity group.
     * @param item The item stack that will act as the icon for this entity group.
     * @param force Whether or not this registration should override any pre-existing icon for the same entity group.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerIconStack (EntityGroup type, @Nullable ItemStack item, boolean force) {
        return BorderRegistry.registerItem(type, item, force);
    }

    /**
     * Registers a portrait for an entity group. This will affect the HUD health bar.
     * Only one portrait may be registered per entity group.
     *
     * Setting resource = null means that the default portrait will be rendered.
     *
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     *
     * @param entityGroup The entity group.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @return Whether or not the registry succeeded. This is false if the entity group already has a portrait.
     */
    public default boolean registerPortrait (EntityGroup entityGroup, @Nullable Identifier resource) {
        return this.registerPortrait(entityGroup, resource, false);
    }

    /**
     * Registers a portrait for an entity group. This will affect the HUD health bar.
     * Only one portrait may be registered per entity group.
     *
     * Setting resource = null means that the default portrait will be rendered.
     *
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     *
     * @param entityGroup The entity group.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @param force Whether or not this registration should override any pre-existing portrait for the same entity group.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerPortrait (EntityGroup entityGroup, @Nullable Identifier resource, boolean force) {
        return BorderRegistry.registerBorder(entityGroup, resource, force);
    }

    /**
     * Sets the HUD portrait frame (imagery around the paper doll) for a certain entity type.
     * If available, a portrait for an entity type will suppress the portrait for an entity group.
     * Only one portrait can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * Setting resource = null means that the default portrait will be rendered.
     *
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     *
     * @param type The entity type.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @return Whether or not the registry succeeded. This is false if the entity type already has a portrait.
     */
    public default boolean registerPortrait (EntityType<?> type, @Nullable Identifier resource) {
        return this.registerPortrait(type, resource, false);
    }

    /**
     * Sets the HUD portrait frame (imagery around the paper doll) for a certain entity type.
     * If available, a portrait for an entity type will suppress the portrait for an entity group.
     * Only one portrait can be registered per entity type. This registry may suppress or be suppressed by other mods.
     *
     * Setting resource = null means that the default portrait will be rendered.
     *
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     *
     * @param type The entity type.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @param force Whether or not this registration should override any pre-existing portrait for the same entity type.
     * @return Whether or not the registry succeeded. Expect this to be true when force = true.
     */
    public default boolean registerPortrait (EntityType<?> type, @Nullable Identifier resource, boolean force) {
        return BorderRegistry.registerBorder(type, resource, force);
    }
}
