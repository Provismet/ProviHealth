package com.provismet.provihealth;

import com.provismet.provihealth.api.ProviHealthApi;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.ElementRegistry;
import com.provismet.provihealth.hud.TargetHealthBar;
import com.provismet.provihealth.particle.Particles;
import com.provismet.provihealth.util.StatusEffectIdentifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviHealthClient implements ClientModInitializer {
    public static final String MODID = "provihealth";
    public static final Logger LOGGER = LoggerFactory.getLogger("Provi's Health Bars");

    public static Identifier identifier (String path) {
        return Identifier.of(MODID, path);
    }

    @Override
    public void onInitializeClient () {
        FabricLoader.getInstance().getModContainer(MODID).ifPresent(container -> {
            ResourceLoader.registerBuiltinPack(identifier("rounded_bars"), container, Text.translatable("resource.provihealth.rounded_bars"), PackActivationType.NORMAL);
            ResourceLoader.registerBuiltinPack(identifier("square_bars"), container, Text.translatable("resource.provihealth.square_bars"), PackActivationType.NORMAL);
        });

        HudElementRegistry.addLast(TargetHealthBar.HEALTHBAR_LAYER, new TargetHealthBar());
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(identifier("asset_listener"), new ElementRegistry());

        FabricLoader.getInstance().getEntrypointContainers(MODID, ProviHealthApi.class).forEach(
            entrypoint -> {
                String otherModId = entrypoint.getProvider().getMetadata().getId();
                try {
                    entrypoint.getEntrypoint().onInitialize();
                }
                catch (Exception e) {
                    LOGGER.error("Mod {} caused an error during inter-mod initialisation: ", otherModId, e);
                }
            }
        );
        ElementRegistry.sortTitles();

        Options.load();
        Particles.init();
        StatusEffectIdentifier.setup();
    }
    
}
