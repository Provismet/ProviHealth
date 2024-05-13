package com.provismet.provihealth.compat;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.api.ProviHealthApi;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.tag.EntityTypeTags;

public class SelfApiHook implements ProviHealthApi {
    private static final int DEFAULT_PRIORITY = -999;

    @Override
    public void onInitialize () {
        this.registerIcon(EntityGroup.AQUATIC, Items.COD);
        this.registerIcon(EntityGroup.ARTHROPOD, Items.COBWEB);
        this.registerIcon(EntityGroup.ILLAGER, Items.IRON_AXE);
        this.registerIcon(EntityGroup.UNDEAD, Items.ROTTEN_FLESH);

        this.registerPortrait(EntityGroup.AQUATIC, ProviHealthClient.identifier("textures/gui/healthbars/aquatic.png"));
        this.registerPortrait(EntityGroup.ARTHROPOD, ProviHealthClient.identifier("textures/gui/healthbars/arthropod.png"));
        this.registerPortrait(EntityGroup.ILLAGER, ProviHealthClient.identifier("textures/gui/healthbars/illager.png"));
        this.registerPortrait(EntityGroup.UNDEAD, ProviHealthClient.identifier("textures/gui/healthbars/undead.png"));
    }
}
