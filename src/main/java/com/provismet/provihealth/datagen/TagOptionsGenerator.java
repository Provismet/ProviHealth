package com.provismet.provihealth.datagen;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.api.TagOptionsDatagenProvider;
import com.provismet.provihealth.config.resources.TagOptions;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.item.Items;
import java.util.concurrent.CompletableFuture;

public class TagOptionsGenerator extends TagOptionsDatagenProvider {
    private static final int DEFAULT_PRIORITY = -999;

    protected TagOptionsGenerator (FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataOutput, registriesFuture);
    }

    @Override
    protected void generate (HolderLookup.Provider lookup, TagConsumer tagConsumer) {
        tagConsumer.add(EntityTypeTags.AQUATIC, new TagOptions(
            DEFAULT_PRIORITY + 1,
            ProviHealthClient.identifier("textures/gui/healthbars/aquatic.png"),
            null,
            Items.COD,
            null
        ));

        tagConsumer.add(EntityTypeTags.ARTHROPOD, new TagOptions(
            DEFAULT_PRIORITY + 2,
            ProviHealthClient.identifier("textures/gui/healthbars/arthropod.png"),
            null,
            Items.COBWEB,
            null
        ));

        tagConsumer.add(EntityTypeTags.ILLAGER, new TagOptions(
            DEFAULT_PRIORITY,
            ProviHealthClient.identifier("textures/gui/healthbars/illager.png"),
            null,
            Items.IRON_AXE,
            null
        ));

        tagConsumer.add(EntityTypeTags.UNDEAD, new TagOptions(
            DEFAULT_PRIORITY + 3,
            ProviHealthClient.identifier("textures/gui/healthbars/undead.png"),
            null,
            Items.ROTTEN_FLESH,
            null
        ));
    }
}
