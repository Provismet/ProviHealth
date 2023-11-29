package com.provismet.provihealth.compat;

import com.provismet.provihealth.api.ProviHealthApi;
import com.provismet.provihealth.hud.BorderRegistry;

import net.minecraft.entity.EntityGroup;
import net.minecraft.item.Items;

public class SelfApiHook implements ProviHealthApi {
    @Override
    public void onInitialize () {
        BorderRegistry.registerItem(EntityGroup.AQUATIC, Items.COD);
        BorderRegistry.registerItem(EntityGroup.ARTHROPOD, Items.COBWEB);
        BorderRegistry.registerItem(EntityGroup.ILLAGER, Items.IRON_AXE);
        BorderRegistry.registerItem(EntityGroup.UNDEAD, Items.ROTTEN_FLESH);
    }
    
}
