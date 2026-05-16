package com.provismet.provihealth.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.resources.EntityOptions;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class EntityOptionsDatagenProvider implements DataProvider {
    private final CompletableFuture<HolderLookup.Provider> future;
    private final PackOutput.PathProvider pathResolver;

    protected EntityOptionsDatagenProvider (FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.future = registriesFuture;
        this.pathResolver = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, ProviHealthClient.MODID + "/entity");
    }

    @Override
    public CompletableFuture<?> run (CachedOutput writer) {
        return this.future.thenCompose(wrapperLookup -> {
            EntityOptionsConsumer consumer = new EntityOptionsConsumer(wrapperLookup);
            this.generate(consumer);

            return CompletableFuture.allOf(consumer.entries.entrySet()
                .stream()
                .map(entry -> {
                    Path path = this.pathResolver.json(entry.getKey());
                    return DataProvider.saveStable(writer, entry.getValue(), path);
                })
                .toArray(CompletableFuture[]::new)
            );
        });
    }

    protected abstract void generate (EntityOptionsConsumer entityConsumer);

    @Override
    public String getName () {
        return "ProviHealth Entity Options";
    }

    protected static class EntityOptionsConsumer {
        private final Map<Identifier, JsonElement> entries = new HashMap<>();
        private final HolderLookup.Provider lookup;

        private EntityOptionsConsumer (HolderLookup.Provider lookup) {
            this.lookup = lookup;
        }

        public void add (EntityType<?> type, EntityOptions options) {
            Optional<ResourceKey<EntityType<?>>> optionalKey = BuiltInRegistries.ENTITY_TYPE.getResourceKey(type);
            if (optionalKey.isEmpty()) throw new RuntimeException("Registry key not found for entity type: " + type.toString());

            DataResult<JsonElement> json = EntityOptions.CODEC.encodeStart(lookup.createSerializationContext(JsonOps.INSTANCE), options);
            this.entries.put(optionalKey.get().identifier(), json.mapError(message -> "Invalid entry for entity type %s: %s".formatted(optionalKey.get().identifier().toString(), message)).getOrThrow());
        }
    }
}
