package com.provismet.provihealth.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.api.ProviHealthApi;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.resources.EntityOptions;
import com.provismet.provihealth.config.resources.TagOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ElementRegistry implements ResourceManagerReloadListener {
    // Cached Elements
    private static final Map<EntityType<?>, Identifier> borderCache = new HashMap<>();
    private static final Map<EntityType<?>, ItemStack> iconCache = new HashMap<>();
    private static final Map<EntityType<?>, Identifier> barCache = new HashMap<>();
    private static final Map<EntityType<?>, Options.HUDType> hudCache = new HashMap<>();
    private static final Map<EntityType<?>, EntityOptions> entityOptionCache = new HashMap<>(); // Preferred
    private static final Map<TagKey<EntityType<?>>, TagOptions> tagOptionsCache = new HashMap<>(); // Used to feed the others data

    // Prioritised HUD elements from dependent mods using the API
    private static final Map<TagKey<EntityType<?>>, BorderPriority> tagBorderPriorities = new HashMap<>();
    private static final Map<TagKey<EntityType<?>>, ItemPriority> tagIconPriorities = new HashMap<>();
    private static final Map<EntityType<?>, BorderPriority> typeBorderPriorities = new HashMap<>();
    private static final Map<EntityType<?>, ItemPriority> typeIconPriorities = new HashMap<>();

    private static final List<TitlePriority> orderedTitles = new ArrayList<>();

    public static final Identifier DEFAULT_BORDER = ProviHealthClient.identifier("textures/gui/healthbars/default.png");
    public static final Identifier DEFAULT_BARS = ProviHealthClient.identifier("textures/gui/healthbars/bars.png");

    @Override
    public void onResourceManagerReload (ResourceManager manager) {
        entityOptionCache.clear();
        tagOptionsCache.clear();
        borderCache.clear();
        iconCache.clear();

        BuiltInRegistries.ENTITY_TYPE.entrySet().forEach(entry -> {
            Identifier resourceLocation = entry.getKey().identifier().withPrefix(ProviHealthClient.MODID + "/entity/").withSuffix(".json");
            Optional<Resource> resource = manager.getResource(resourceLocation);
            if (resource.isPresent()) {
                try (InputStream stream = resource.get().open()) {
                    String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                    DataResult<Pair<EntityOptions, JsonElement>> dataResult = EntityOptions.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(text));
                    EntityOptions resolvedOptions = dataResult.getOrThrow().getFirst();
                    entityOptionCache.put(entry.getValue(), resolvedOptions);
                    ProviHealthClient.LOGGER.info("Found entity option with ID: {}", resourceLocation);
                }
                catch (Throwable e) {
                    ProviHealthClient.LOGGER.error("ProviHealth encountered an error reading file {} from pack {}", resourceLocation, resource.get().sourcePackId(), e);
                }
            }
        });

        Map<Identifier, Resource> tagOptions = manager.listResources(ProviHealthClient.MODID + "/tag", identifier -> identifier.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> tagEntry : tagOptions.entrySet()) {
            String path = tagEntry.getKey().getPath().replace(".json", "").replaceFirst("provihealth/tag/", "");
            Identifier tagId = Identifier.fromNamespaceAndPath(tagEntry.getKey().getNamespace(), path);
            TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tagId);
            ProviHealthClient.LOGGER.info("Found tag options file {} for entity tag {}", tagEntry.getKey(), tagId);

            try (InputStream stream = tagEntry.getValue().open()) {
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                DataResult<Pair<TagOptions, JsonElement>> dataResult = TagOptions.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(text));
                TagOptions resolvedOptions = dataResult.getOrThrow().getFirst();
                tagOptionsCache.put(tagKey, resolvedOptions);
            }
            catch (Throwable e) {
                ProviHealthClient.LOGGER.error("ProviHealth encountered an error reading file {} from pack {}", tagEntry.getKey(), tagEntry.getValue().sourcePackId(), e);
            }
        }
    }

    public static boolean registerBorder (TagKey<EntityType<?>> entityTag, @Nullable Identifier border, int priority) {
        if (entityTag == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null object to the border registry.");
            return false;
        }
        else if (tagBorderPriorities.containsKey(entityTag) && priority <= tagBorderPriorities.get(entityTag).priority()) {
            return false;
        }
        tagBorderPriorities.put(entityTag, new BorderPriority(border, priority));
        return true;
    }

    public static boolean registerItem (TagKey<EntityType<?>> entityTag, @Nullable ItemStack item, int priority) {
        if (entityTag == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null EntityGroup to the icon registry.");
            return false;
        }
        else if (tagIconPriorities.containsKey(entityTag) && priority <= tagIconPriorities.get(entityTag).priority()) {
            return false;
        }
        tagIconPriorities.put(entityTag, new ItemPriority(item, priority));
        return true;
    }

    public static boolean registerBorder (EntityType<?> type, @Nullable Identifier border, int priority) {
        if (type == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null EntityType to the border registry.");
            return false;
        }
        else if (typeBorderPriorities.containsKey(type) && priority <= typeBorderPriorities.get(type).priority()) {
            return false;
        }
        typeBorderPriorities.put(type, new BorderPriority(border, priority));
        return true;
    }

    public static boolean registerItem (EntityType<?> type, @Nullable ItemStack item, int priority) {
        if (type == null) {
            ProviHealthClient.LOGGER.error("Attempted to register a null EntityType to the icon registry.");
            return false;
        }
        else if (typeIconPriorities.containsKey(type) && priority <= typeIconPriorities.get(type).priority()) {
            return false;
        }
        typeIconPriorities.put(type, new ItemPriority(item, priority));
        return true;
    }

    public static void registerTitle (ProviHealthApi.TitleGenerator titleGen, int order) {
        orderedTitles.add(new TitlePriority(titleGen, order));
    }

    public static void sortTitles () {
        orderedTitles.sort(Comparator.comparingInt(TitlePriority::order));
    }

    public static EntityOptions getEntityOptions (@Nullable LivingEntity entity) {
        if (entity == null) return EntityOptions.DEFAULT;
        return entityOptionCache.getOrDefault(entity.getType(), EntityOptions.DEFAULT);
    }

    // Only used as a fallback when EntityOptions doesn't have it.
    @NotNull
    public static Identifier getOrCacheBorder (@Nullable LivingEntity entity) {
        if (entity == null || !Options.useCustomHudPortraits) return DEFAULT_BORDER;
        else {
            if (borderCache.containsKey(entity.getType())) return borderCache.get(entity.getType());

            int maxPriority = Integer.MIN_VALUE;
            Identifier bestBorder = DEFAULT_BORDER;
            // Read from assets
            for (Map.Entry<TagKey<EntityType<?>>, TagOptions> entry : tagOptionsCache.entrySet()) {
                if (entity.is(entry.getKey()) && entry.getValue().getPriority() > maxPriority && entry.getValue().getBorder() != null) {
                    bestBorder = entry.getValue().getBorder();
                    maxPriority = entry.getValue().getPriority();
                }
            }

            // Read from mod addons
            for (TagKey<EntityType<?>> entityTag : tagBorderPriorities.keySet()) {
                if (entity.is(entityTag) && tagBorderPriorities.get(entityTag).priority() > maxPriority) {
                    bestBorder = tagBorderPriorities.get(entityTag).borderId();
                    maxPriority = tagBorderPriorities.get(entityTag).priority();
                }
            }

            for (EntityType<?> type : typeBorderPriorities.keySet()) {
                if (entity.getType() == type && typeBorderPriorities.get(type).priority() > maxPriority) {
                    bestBorder = typeBorderPriorities.get(type).borderId();
                    maxPriority = typeBorderPriorities.get(type).priority();
                }
            }

            borderCache.put(entity.getType(), bestBorder);
            return bestBorder;
        }
    }

    // Only used as a fallback when EntityOptions doesn't have it.
    @Nullable
    public static ItemStack getOrCacheIcon (LivingEntity entity) {
        if (entity == null) return null;
        else if (iconCache.containsKey(entity.getType())) return iconCache.get(entity.getType());

        ItemStack bestIcon = null;
        int maxPriority = Integer.MIN_VALUE;

        // Read from assets
        for (Map.Entry<TagKey<EntityType<?>>, TagOptions> entry : tagOptionsCache.entrySet()) {
            if (entity.is(entry.getKey()) && entry.getValue().getPriority() > maxPriority && entry.getValue().getIcon() != null) {
                bestIcon = entry.getValue().getIcon();
                maxPriority = entry.getValue().getPriority();
            }
        }

        // Read from mod addons
        for (TagKey<EntityType<?>> entityTag : tagIconPriorities.keySet()) {
            if (entity.is(entityTag) && tagIconPriorities.get(entityTag).priority() > maxPriority) {
                bestIcon = tagIconPriorities.get(entityTag).itemStack();
                maxPriority = tagIconPriorities.get(entityTag).priority();
            }
        }

        for (EntityType<?> type : typeIconPriorities.keySet()) {
            if (entity.getType() == type && typeIconPriorities.get(type).priority() > maxPriority) {
                bestIcon = typeIconPriorities.get(type).itemStack();
                maxPriority = typeIconPriorities.get(type).priority();
            }
        }
        iconCache.put(entity.getType(), bestIcon);
        return bestIcon;
    }

    @NotNull
    public static Identifier getOrCacheHealthBar (LivingEntity entity) {
        if (entity == null) return DEFAULT_BARS;
        if (barCache.containsKey(entity.getType())) return barCache.get(entity.getType());

        Identifier bestBars = DEFAULT_BARS;
        int maxPriority = Integer.MIN_VALUE;

        // Read from assets
        for (Map.Entry<TagKey<EntityType<?>>, TagOptions> entry : tagOptionsCache.entrySet()) {
            if (entity.is(entry.getKey()) && entry.getValue().getPriority() > maxPriority && entry.getValue().getHealthBar() != null) {
                bestBars = entry.getValue().getHealthBar();
                maxPriority = entry.getValue().getPriority();
            }
        }

        barCache.put(entity.getType(), bestBars);
        return bestBars;
    }

    @Nullable
    public static Options.HUDType getOrCacheHudType (LivingEntity entity) {
        if (entity == null) return null;
        if (hudCache.containsKey(entity.getType())) return hudCache.get(entity.getType());

        Options.HUDType bestHud = null;
        int maxPriority = Integer.MIN_VALUE;

        // Read from assets
        for (Map.Entry<TagKey<EntityType<?>>, TagOptions> entry : tagOptionsCache.entrySet()) {
            if (entity.is(entry.getKey()) && entry.getValue().getPriority() > maxPriority && entry.getValue().getHudType() != null) {
                bestHud = entry.getValue().getHudType();
                maxPriority = entry.getValue().getPriority();
            }
        }

        hudCache.put(entity.getType(), bestHud);
        return bestHud;
    }

    public static List<Component> getTitle (LivingEntity entity, boolean world, boolean hud) {
        if (entity == null) return null;

        return orderedTitles.stream().map(title -> title.titleGetter().apply(entity, world, hud)).filter(Objects::nonNull).toList();
    }

    private record ItemPriority (ItemStack itemStack, int priority) {}
    private record BorderPriority (Identifier borderId, int priority) {}
    private record TitlePriority (ProviHealthApi.TitleGenerator titleGetter, int order) {}
}
