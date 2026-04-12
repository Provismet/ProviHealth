package com.provismet.provihealth.compat;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.api.ProviHealthApi;

public class SelfApiHook implements ProviHealthApi {
    private static final int DEFAULT_PRIORITY = -999;

    @Override
    public void onInitialize () {
//        this.registerIcon(EntityTypeTags.AQUATIC, Items.COD, DEFAULT_PRIORITY + 1);
//        this.registerIcon(EntityTypeTags.ARTHROPOD, Items.COBWEB, DEFAULT_PRIORITY + 2);
//        this.registerIcon(EntityTypeTags.ILLAGER, Items.IRON_AXE, DEFAULT_PRIORITY);
//        this.registerIcon(EntityTypeTags.UNDEAD, Items.ROTTEN_FLESH, DEFAULT_PRIORITY + 3);
//
//        this.registerPortrait(EntityTypeTags.AQUATIC, ProviHealthClient.identifier("textures/gui/healthbars/aquatic.png"), DEFAULT_PRIORITY + 1);
//        this.registerPortrait(EntityTypeTags.ARTHROPOD, ProviHealthClient.identifier("textures/gui/healthbars/arthropod.png"), DEFAULT_PRIORITY + 2);
//        this.registerPortrait(EntityTypeTags.ILLAGER, ProviHealthClient.identifier("textures/gui/healthbars/illager.png"), DEFAULT_PRIORITY);
//        this.registerPortrait(EntityTypeTags.UNDEAD, ProviHealthClient.identifier("textures/gui/healthbars/undead.png"), DEFAULT_PRIORITY + 3);
    }
}
