package com.provismet.provihealth.datagen;

import com.provismet.lilylib.datagen.provider.LilyLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

public class LanguageGenerator extends LilyLanguageProvider {
    protected LanguageGenerator (FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup, String languageCode) {
        super(dataOutput, languageCode, registryLookup);
    }

    protected LanguageGenerator (FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations (HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        this.prefixed(translationBuilder, "title", "config", "Provi's Health Bars");
        
        this.category(translationBuilder, "general", "General");
        this.category(translationBuilder, "hud", "HUD");
        this.category(translationBuilder, "health", "In-World");
        this.category(translationBuilder, "particles", "Particles");
        this.category(translationBuilder, "compat", "Compatibility");
        
        this.subcategory(translationBuilder, "boss", "Bosses");
        this.subcategory(translationBuilder, "hostile", "Hostile Mobs");
        this.subcategory(translationBuilder, "player", "Players");
        this.subcategory(translationBuilder, "other", "Non-Hostile Mobs");

        this.entry(translationBuilder, "hudDuration", "HUD Duration");
        this.entry(translationBuilder, "blacklist", "Blacklist");
        this.entry(translationBuilder, "boss", "Boss Display");
        this.entry(translationBuilder, "hostile", "Hostile Mob Display");
        this.entry(translationBuilder, "player", "Player Display");
        this.entry(translationBuilder, "other", "Non-Hostile Mob Display");
        this.entry(translationBuilder, "healthBar", "Health Bar Visibility");
        this.entry(translationBuilder, "targetOverride", "Show On Target");
        this.entry(translationBuilder, "hudGlide", "Glide Speed");
        this.entry(translationBuilder, "showIcon", "Show Group Icon");
        this.entry(translationBuilder, "hudPortraits", "Use Custom HUD Portraits");
        this.entry(translationBuilder, "worldText", "Show Health Value");
        this.entry(translationBuilder, "maxDistance", "Maximum Render Distance");
        this.entry(translationBuilder, "barScale", "Bar Size");
        this.entry(translationBuilder, "hudOffsetY", "HUD Offset Y");
        this.entry(translationBuilder, "hudTitles", "Show Titles On HUD");
        this.entry(translationBuilder, "damageParticles", "Show Damage Particles");
        this.entry(translationBuilder, "healingParticles", "Show Healing Particles");
        this.entry(translationBuilder, "damageColour", "Damage Particle Color");
        this.entry(translationBuilder, "healingColour", "Healing Particle Color");
        this.entry(translationBuilder, "particleScale", "Particle Size");
        this.entry(translationBuilder, "particleTextShadow", "Text Shadow");
        this.entry(translationBuilder, "damageParticleTextColour", "Damage Text Color");
        this.entry(translationBuilder, "healingParticleTextColour", "Healing Text Color");
        this.entry(translationBuilder, "particleType", "Particle Movement");
        this.entry(translationBuilder, "maxParticleDistance", "Particle Render Distance");
        this.entry(translationBuilder, "hudOffsetX", "HUD Position");
        this.entry(translationBuilder, "gradient", "Dynamic Bar Color");
        this.entry(translationBuilder, "barStartColour", "Full Health Colour");
        this.entry(translationBuilder, "barEndColour", "Low Health Colour");
        this.entry(translationBuilder, "overrideLabels", "Replace Nameplates");
        this.entry(translationBuilder, "worldShadows", "Text Shadow");
        this.entry(translationBuilder, "compatText", "In-World Health Bar SeeThrough Text Mode");
        this.entry(translationBuilder, "compatWorld", "In-World Health Bar Shader Compatibility");
        this.entry(translationBuilder, "worldOffsetY", "Health Bar Offset Y");
        this.entry(translationBuilder, "worldTitles", "Show Titles In World");
        this.entry(translationBuilder, "damageAlpha", "Damage Particle Alpha");
        this.entry(translationBuilder, "healingAlpha", "Healing Particle Alpha");
        this.entry(translationBuilder, "compatHud", "HUD Paperdoll Render Mode");
        this.entry(translationBuilder, "hudStatuses", "Show Status Effects");
        this.entry(translationBuilder, "tintBackground", "Tint Background");
        this.entry(translationBuilder, "teamColours", "Use Team Colors");

        this.enumEntry(translationBuilder, "full", "Full");
        this.enumEntry(translationBuilder, "portrait_only", "Portrait Only");
        this.enumEntry(translationBuilder, "none", "None");

        this.enumEntry(translationBuilder, "always_show", "Show");
        this.enumEntry(translationBuilder, "hide_if_full", "Hide When Full");
        this.enumEntry(translationBuilder, "always_hide", "Hide");

        this.enumEntry(translationBuilder, "rising", "Rising");
        this.enumEntry(translationBuilder, "gravity", "Has Gravity");
        this.enumEntry(translationBuilder, "static", "None");

        this.enumEntry(translationBuilder, "left", "Left");
        this.enumEntry(translationBuilder, "right", "Right");

        this.enumEntry(translationBuilder, "seethroughtext", "standard", "SeeThrough Text, Normal Shadows");
        this.enumEntry(translationBuilder, "seethroughtext", "none", "Normal Text/Shadows");
        this.enumEntry(translationBuilder, "seethroughtext", "full", "SeeThrough Text/Shadows");

        this.enumEntry(translationBuilder, "hudportraitcompatmode", "standard", "Standard");
        this.enumEntry(translationBuilder, "hudportraitcompatmode", "compat", "Compatibility");
        this.enumEntry(translationBuilder, "hudportraitcompatmode", "none", "None");

        this.tooltip(translationBuilder, "hudDuration", "How many ticks the HUD display will linger for when not targeting a mob.");
        this.tooltip(translationBuilder, "targetOverride", "Always render health bar when targeting a mob of this type.");
        this.tooltip(translationBuilder, "glide", "Determines how quickly the health bar will slide from its old value to new values.\nWarning, Very low values will prevent the health bar visual from catching up with the true value.");
        this.tooltip(translationBuilder, "hudPortraits", "Allows mobs to have different HUD portraits based on their group or type.");
        this.tooltip(translationBuilder, "maxDistance", "Mobs beyond this distance will not render health bars.");
        this.tooltip(translationBuilder, "hudOffsetY", "As a percentage of the window height.");
        this.tooltip(translationBuilder, "gradient", "Whether or not the health bar will change color as it gets lower.\nTurning this off sets the bar color to the starting value.");
        this.tooltip(translationBuilder, "overrideLabels", "When true health bar text will include the name of the mob and nameplates will not be rendered.\nWhen false health bar text will only show the health values, and the bar will move upwards when a nameplate is being rendered.\nWarning, Only the name of the mob is added to the health bar, if a mod/plugin/scoreboard adds additional lines of text to the nameplate then that text will be lost.");
        this.tooltip(translationBuilder, "compatText", "SeeThrough Text is any text that is visible through walls, this includes player nameplates.\nThis setting only applies when Replace Nameplates is active.\nSeeThrough Text-Shadows break easily in both vanilla and heavily modded environments. Change this setting if overridden nameplates render wrong.\nIf this setting does not solve the shadow issue, then disable text-shadows in the In-World menu.");
        this.tooltip(translationBuilder, "compatWorld", "Uses a different rendering program for in-world health bars. Only turn this on if shaders are preventing the bars from rendering.\nWarning, This option disables any color-related settings for in-world health bars.");
        this.tooltip(translationBuilder, "hudTitles", "Allows other mods to show text under the HUD portrait.");
        this.tooltip(translationBuilder, "worldTitles", "Allows other mods to show text above the in-world health bars.");
        this.tooltip(translationBuilder, "tintBackground", "Applies the bar color to the background as well.");
        this.tooltip(translationBuilder, "teamColours", "Matches health bar color to the entity's team if applicable.\nTeam colors will override colors from other settings.");

        translationBuilder.add("entity.provihealth.unknownPlayer", "[Invisible Player]");

        this.resource(translationBuilder, "rounded_bars", "Rounded Bars", "Round-styled healthbars.");
        this.resource(translationBuilder, "square_bars", "Square Bars", "Square-styled healthbars.");
    }

    protected void prefixed (TranslationBuilder builder, String prefix, String name, String translation) {
        builder.add(prefix + ".provihealth." + name, translation);
    }

    protected void category (TranslationBuilder builder, String name, String translation) {
        this.prefixed(builder, "category", name, translation);
    }

    protected void subcategory (TranslationBuilder builder, String name, String translation) {
        this.prefixed(builder, "subcategory", name, translation);
    }

    protected void entry (TranslationBuilder builder, String name, String translation) {
        this.prefixed(builder, "entry", name, translation);
    }

    protected void enumEntry (TranslationBuilder builder, String name, String entry, String translation) {
        this.prefixed(builder, "enum", name + "." + entry, translation);
    }

    protected void enumEntry (TranslationBuilder builder, String name, String translation) {
        this.prefixed(builder, "enum", name, translation);
    }

    protected void tooltip (TranslationBuilder builder, String name, String translation) {
        this.prefixed(builder, "tooltip", name, translation);
    }

    protected void resource (TranslationBuilder builder, String name, String translation, String description) {
        this.prefixed(builder, "resource", name, translation);
        this.prefixed(builder, "resource", name + ".description", description);
    }
}
