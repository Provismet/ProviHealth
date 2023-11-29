package com.provismet.provihealth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.provismet.provihealth.api.ProviHealthApi;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.TargetHealthBar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
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

        FabricLoader.getInstance().getEntrypointContainers(MODID, ProviHealthApi.class).forEach(
            entrypoint -> {
                String otherModId = entrypoint.getProvider().getMetadata().getId();
                try {
                    entrypoint.getEntrypoint().onInitialize();
                }
                catch (Exception e) {
                    LOGGER.error("Mod " + otherModId + " caused an error during inter-mod initialisation: ", e);
                }
            }
        );

        Options.load();
    }
    
}
