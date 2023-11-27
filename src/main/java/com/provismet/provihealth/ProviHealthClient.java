package com.provismet.provihealth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.provismet.provihealth.hud.BorderRegistry;
import com.provismet.provihealth.hud.TargetHealthBar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ProviHealthClient implements ClientModInitializer {
    public static final String MODID = "provihealth";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static Identifier identifier (String path) {
        return Identifier.of(MODID, path);
    }

    @Override
    public void onInitializeClient () {
        HudRenderCallback.EVENT.register(new TargetHealthBar());

        BorderRegistry.registerItem(EntityGroup.AQUATIC, Items.COD);
        BorderRegistry.registerItem(EntityGroup.ARTHROPOD, Items.COBWEB);
        BorderRegistry.registerItem(EntityGroup.ILLAGER, Items.IRON_AXE);
        BorderRegistry.registerItem(EntityGroup.UNDEAD, Items.ROTTEN_FLESH);
    }
    
}
