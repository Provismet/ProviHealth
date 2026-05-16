package com.provismet.provihealth.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class LanguageGeneratorUK extends LanguageGenerator {
    protected LanguageGeneratorUK (FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup, "en_gb");
    }

    @Override
    public void generateTranslations (HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        this.entry(translationBuilder, "damageColour", "Damage Particle Colour");
        this.entry(translationBuilder, "healingColour", "Healing Particle Colour");
        this.entry(translationBuilder, "damageParticleTextColour", "Damage Text Colour");
        this.entry(translationBuilder, "healingParticleTextColour", "Healing Text Colour");
        this.entry(translationBuilder, "barStartColour", "Full Health Colour");
        this.entry(translationBuilder, "barEndColour", "Low Health Colour");
        this.entry(translationBuilder, "teamColours", "Use Team Colours");

        this.tooltip(translationBuilder, "gradient", "Whether or not the health bar will change colour as it gets lower.\nTurning this off sets the bar colour to the starting value.");
        this.tooltip(translationBuilder, "compatWorld", "Uses a different rendering program for in-world health bars. Only turn this on if shaders are preventing the bars from rendering.\nWarning: This option disables any colour-related settings for in-world health bars.");
        this.tooltip(translationBuilder, "tintBackground", "Applies the bar colour to the background as well.");
        this.tooltip(translationBuilder, "teamColours", "Matches health bar colour to the entity's team if applicable.\nTeam colours will override colours from other settings.");
    }
}
