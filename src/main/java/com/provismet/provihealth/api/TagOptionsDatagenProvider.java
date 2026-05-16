package com.provismet.provihealth.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.resources.TagOptions;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class TagOptionsDatagenProvider implements DataProvider {
    private final CompletableFuture<HolderLookup.Provider> future;
    private final PackOutput.PathProvider pathResolver;

    protected TagOptionsDatagenProvider (FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.future = registriesFuture;
        this.pathResolver = dataOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, ProviHealthClient.MODID + "/tag");
    }

    @Override
    public CompletableFuture<?> run (CachedOutput writer) {
        return this.future.thenCompose(wrapperLookup -> {
            TagConsumer consumer = new TagConsumer(wrapperLookup);
            this.generate(wrapperLookup, consumer);

            return CompletableFuture.allOf(consumer.entries.entrySet()
                .stream()
                .map(entry -> {
                    Path path = this.pathResolver.json(entry.getKey().location());
                    return DataProvider.saveStable(writer, entry.getValue(), path);
                })
                .toArray(CompletableFuture[]::new)
            );
        });
    }

    protected abstract void generate (HolderLookup.Provider lookup, TagConsumer tagConsumer);

    @Override
    public String getName () {
        return "ProviHealth Tag Options";
    }

    protected static class TagConsumer {
        private final Map<TagKey<EntityType<?>>, JsonElement> entries = new HashMap<>();
        private final HolderLookup.Provider lookup;

        private TagConsumer (HolderLookup.Provider lookup) {
            this.lookup = lookup;
        }

        public void add (TagKey<EntityType<?>> tag, TagOptions options) {
            DataResult<JsonElement> json = TagOptions.CODEC.encodeStart(lookup.createSerializationContext(JsonOps.INSTANCE), options);
            this.entries.put(tag, json.mapError(message -> "Invalid entry %s: %s".formatted(tag.location(), message)).getOrThrow());
        }
    }
}
