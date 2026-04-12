package com.provismet.provihealth.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.provismet.provihealth.hud.ElementRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Mod integration API for Provi's Health Bars, this API is noticeably less powerful than the data-driven features, but
 * has access to titles, which is not possible with datapacks.
 */
public interface ProviHealthApi {
    void onInitialize ();

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * Only one icon may be registered per entity group.
     * <p>
     * The icon takes the form of an item.
     * 
     * @param tag The entity type tag.
     * @param item The item to serve as the icon.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIcon (TagKey<EntityType<?>> tag, @NotNull Item item) {
        return this.registerIcon(tag, item, 0);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * Only one icon may be registered per entity group.
     * <p>
     * The icon takes the form of an item.
     * 
     * @param tag The entity type tag.
     * @param item The item to serve as the icon.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIcon (TagKey<EntityType<?>> tag, @NotNull Item item, int priority) {
        return ElementRegistry.registerItem(tag, item.getDefaultInstance(), priority);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type. 
     * If available, icons for entity types will suppress icons for entity groups.
     * <p>
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity type.
     * @param item The item that will act as the icon for this entity type.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIcon (EntityType<?> type, @NotNull Item item) {
        return this.registerIcon(type, item, 0);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type. 
     * If available, icons for entity types will suppress icons for entity groups.
     * <p>
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity type.
     * @param item The item that will act as the icon for this entity type.
     * @param priority The priority level of this registration. Higher priority icons will override lower priority icons.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIcon (EntityType<?> type, @NotNull Item item, int priority) {
        return ElementRegistry.registerItem(type, item.getDefaultInstance(), priority);
    }


    /**
     * Sets an icon to appear on the HUD for a certain entity type. This method accepts ItemStacks with NBT.
     * If available, icons for entity types will suppress icons for entity groups.
     * <p>
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     * <p>
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity type.
     * @param item The item stack that will act as the icon for this entity type.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIconStack (EntityType<?> type, @Nullable ItemStack item) {
        return this.registerIconStack(type, item, 0);
    }

    /**
     * Sets an icon to appear on the HUD for a certain entity type. This method accepts ItemStacks with NBT.
     * If available, icons for entity types will suppress icons for entity groups.
     * <p>
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     * <p>
     * Only one icon can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity type.
     * @param item The item stack that will act as the icon for this entity type.
     * @param priority The priority level of this registration. Higher priority icons will override lower priority icons.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIconStack (EntityType<?> type, @Nullable ItemStack item, int priority) {
        return ElementRegistry.registerItem(type, item, priority);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * This method accepts an ItemStack with NBT. Only one icon may be registered per entity group.
     * <p>
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     * <p>
     * Only one icon can be registered per entity group. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity group.
     * @param item The item stack that will act as the icon for this entity group.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIconStack (TagKey<EntityType<?>> type, @Nullable ItemStack item) {
        return this.registerIconStack(type, item, 0);
    }

    /**
     * Registers an icon that will display on top of the health bar in the HUD for a specific entity group.
     * This method accepts an ItemStack with NBT. Only one icon may be registered per entity group.
     * <p>
     * Setting item = null means no item will be rendered. This is still considered a valid registry.
     * <p>
     * Only one icon can be registered per entity group. This registry may suppress or be suppressed by other mods.
     * 
     * @param type The entity group.
     * @param item The item stack that will act as the icon for this entity group.
     * @param priority The priority level of this registration. Higher priority icons will override lower priority icons.
     * @return Whether or not the registry succeeded. This is false if a higher priority icon already exists.
     */
    default boolean registerIconStack (TagKey<EntityType<?>> type, @Nullable ItemStack item, int priority) {
        return ElementRegistry.registerItem(type, item, priority);
    }

    /**
     * Registers a portrait for an entity group. This will affect the HUD health bar.
     * Only one portrait may be registered per entity group.
     * <p>
     * Setting resource = null means that the default portrait will be rendered.
     * <p>
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     * 
     * @param tag The entity group.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @return Whether or not the registry succeeded. This is false if a higher priority portrait already exists.
     */
    default boolean registerPortrait (TagKey<EntityType<?>> tag, @Nullable Identifier resource) {
        return this.registerPortrait(tag, resource, 0);
    }

    /**
     * Registers a portrait for an entity group. This will affect the HUD health bar.
     * Only one portrait may be registered per entity group.
     * <p>
     * Setting resource = null means that the default portrait will be rendered.
     * <p>
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     * 
     * @param entityGroup The entity group.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @param priority The priority level of this registration. Higher priority portraits will override lower priority portraits.
     * @return Whether or not the registry succeeded. This is false if a higher priority portrait already exists.
     */
    default boolean registerPortrait (TagKey<EntityType<?>> entityGroup, @Nullable Identifier resource, int priority) {
        return ElementRegistry.registerBorder(entityGroup, resource, priority);
    }

    /**
     * Sets the HUD portrait frame (imagery around the paper doll) for a certain entity type.
     * If available, a portrait for an entity type will suppress the portrait for an entity group.
     * Only one portrait can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * <p>
     * Setting resource = null means that the default portrait will be rendered.
     * <p>
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     * 
     * @param type The entity type.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @return Whether or not the registry succeeded. This is false if a higher priority portrait already exists.
     */
    default boolean registerPortrait (EntityType<?> type, @Nullable Identifier resource) {
        return this.registerPortrait(type, resource, 0);
    }

    /**
     * Sets the HUD portrait frame (imagery around the paper doll) for a certain entity type.
     * If available, a portrait for an entity type will suppress the portrait for an entity group.
     * Only one portrait can be registered per entity type. This registry may suppress or be suppressed by other mods.
     * <p>
     * Setting resource = null means that the default portrait will be rendered.
     * <p>
     * If your mod introduces new entity groups, use this method to define a portrait for them.
     * The image for the frame must be 96x48 in size. With the foreground (48x48) on the left and the background (48x48) on the right.
     * 
     * @param type The entity type.
     * @param resource A full resource path (modid:textures/gui/etc/file.png) to the texture.
     * @param priority The priority level of this registration. Higher priority portraits will override lower priority portraits.
     * @return Whether or not the registry succeeded. This is false if a higher priority portrait already exists.
     */
    default boolean registerPortrait (EntityType<?> type, @Nullable Identifier resource, int priority) {
        return ElementRegistry.registerBorder(type, resource, priority);
    }

    /**
     * Registers a function that places text on the healthbars or the HUD.
     * Text can be used to place additional information about an entity on the HUD or the in-world healthbar.
     * Text appears line-by-line on the HUD and in-world healthbar. Separate lines should be registered separately.
     * The lambda is given context for whether it is appearing on the in-world or the HUD render.
     *
     * @param titleLambda Function of (LivingEntity entity, boolean isWorld, boolean isHUD) -> Text. Should return null if no text is wanted.
     * @param order Determines the order of this line of text. Higher numbers appear at the top.
     */
    default void registerTitle (TitleGenerator titleLambda, int order) {
        ElementRegistry.registerTitle(titleLambda, order);
    }

    @FunctionalInterface
    interface TitleGenerator {
        Component apply (LivingEntity entity, boolean isWorld, boolean isHUD);
    }
}
