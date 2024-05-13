package com.provismet.provihealth.compat;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.DamageParticleType;
import com.provismet.provihealth.config.Options.HUDPortraitCompatMode;
import com.provismet.provihealth.config.Options.HUDPosition;
import com.provismet.provihealth.config.Options.HUDType;
import com.provismet.provihealth.config.Options.SeeThroughText;
import com.provismet.provihealth.config.Options.VisibilityType;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Arrays;

public class ProviHealthConfigScreen {
    public static Screen build (Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create();
        builder.setParentScreen(parent);
        builder.setTitle(Text.translatable("title.provihealth.config"));
        builder.setDefaultBackgroundTexture(new Identifier("textures/block/deepslate_tiles.png"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("category.provihealth.hud"));
        ConfigCategory health = builder.getOrCreateCategory(Text.translatable("category.provihealth.health"));
        ConfigCategory particles = builder.getOrCreateCategory(Text.translatable("category.provihealth.particles"));
        ConfigCategory compatibility = builder.getOrCreateCategory(Text.translatable("category.provihealth.compat"));

        hud.addEntry(entryBuilder.startIntField(Text.translatable("entry.provihealth.hudDuration"), Options.maxHealthBarTicks)
            .setDefaultValue(40)
            .setMin(0)
            .setTooltip(Text.translatable("tooltip.provihealth.hudDuration"))
            .setSaveConsumer(newValue -> Options.maxHealthBarTicks = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.showIcon"), Options.showHudIcon)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> Options.showHudIcon = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.hudPortraits"), Options.useCustomHudPortraits)
            .setDefaultValue(true)
            .setTooltip(Text.translatable("tooltip.provihealth.hudPortraits"))
            .setSaveConsumer(newValue -> Options.useCustomHudPortraits = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.hudGlide"), Options.hudGlide)
            .setDefaultValue(0.5f)
            .setMin(0.01f)
            .setMax(1f)
            .setTooltip(Text.translatable("tooltip.provihealth.glide"))
            .setSaveConsumer(newValue -> Options.hudGlide = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.hudOffsetX"), HUDPosition.class, Options.hudPosition)
            .setDefaultValue(HUDPosition.LEFT)
            .setSaveConsumer(newValue -> Options.hudPosition = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startIntSlider(Text.translatable("entry.provihealth.hudOffsetY"), Options.hudOffsetPercent, 0, 100)
            .setDefaultValue(0)
            .setTooltip(Text.translatable("tooltip.provihealth.hudOffsetY"))
            .setSaveConsumer(newValue -> Options.hudOffsetPercent = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.gradient"), Options.hudGradient)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("tooltip.provihealth.gradient"))
            .setSaveConsumer(newValue -> Options.hudGradient = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.barStartColour"), Options.hudStartColour)
            .setDefaultValue(0x00C100)
            .setSaveConsumer(newValue -> {
                Options.hudStartColour = newValue;
                Options.unpackedStartHud = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        hud.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.barEndColour"), Options.hudEndColour)
            .setDefaultValue(0xFF0000)
            .setSaveConsumer(newValue -> {
                Options.hudEndColour = newValue;
                Options.unpackedEndHud = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        hud.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.boss"), HUDType.class, Options.bossHUD)
            .setDefaultValue(HUDType.FULL)
            .setSaveConsumer(newValue -> Options.bossHUD = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.hostile"), HUDType.class, Options.hostileHUD)
            .setDefaultValue(HUDType.FULL)
            .setSaveConsumer(newValue -> Options.hostileHUD = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.player"), HUDType.class, Options.playerHUD)
            .setDefaultValue(HUDType.FULL)
            .setSaveConsumer(newValue -> Options.playerHUD = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.other"), HUDType.class, Options.otherHUD)
            .setDefaultValue(HUDType.FULL)
            .setSaveConsumer(newValue -> Options.otherHUD = newValue)
            .build()
        );

        hud.addEntry(entryBuilder.startStrList(Text.translatable("entry.provihealth.blacklist"), Options.blacklistHUD)
            .setDefaultValue(Arrays.asList("minecraft:armor_stand"))
            .setSaveConsumer(newValue -> Options.blacklistHUD = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.overrideLabels"), Options.overrideLabels)
            .setDefaultValue(false)
            .setSaveConsumer(newValue -> Options.overrideLabels = newValue)
            .setTooltip(Text.translatable("tooltip.provihealth.overrideLabels"))
            .build()
        );

        health.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.hudGlide"), Options.worldGlide)
            .setDefaultValue(0.5f)
            .setMin(0.01f)
            .setMax(1f)
            .setTooltip(Text.translatable("tooltip.provihealth.glide"))
            .setSaveConsumer(newValue -> Options.worldGlide = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.worldText"), Options.showTextInWorld)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> Options.showTextInWorld = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.worldShadows"), Options.worldShadows)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> Options.worldShadows = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.maxDistance"), Options.maxRenderDistance)
            .setMin(0f)
            .setDefaultValue(24f)
            .setTooltip(Text.translatable("tooltip.provihealth.maxDistance"))
            .setSaveConsumer(newValue -> Options.maxRenderDistance = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.barScale"), Options.worldHealthBarScale)
            .setMin(0.01f)
            .setMax(3f)
            .setDefaultValue(1.5f)
            .setSaveConsumer(newValue -> Options.worldHealthBarScale = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.worldOffsetY"), Options.worldOffsetY)
            .setDefaultValue(0f)
            .setSaveConsumer(newValue -> Options.worldOffsetY = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.gradient"), Options.worldGradient)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("tooltip.provihealth.gradient"))
            .setSaveConsumer(newValue -> Options.worldGradient = newValue)
            .build()
        );

        health.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.barStartColour"), Options.worldStartColour)
            .setDefaultValue(0x00C100)
            .setSaveConsumer(newValue -> {
                Options.worldStartColour = newValue;
                Options.unpackedStartWorld = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        health.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.barEndColour"), Options.worldEndColour)
            .setDefaultValue(0xFF0000)
            .setSaveConsumer(newValue -> {
                Options.worldEndColour = newValue;
                Options.unpackedEndWorld = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        health.addEntry(entryBuilder.startSubCategory(Text.translatable("subcategory.provihealth.boss"), Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.healthBar"), VisibilityType.class, Options.bosses)
                    .setDefaultValue(VisibilityType.ALWAYS_HIDE)
                    .setSaveConsumer(newValue -> Options.bosses = newValue)
                    .build(),
                entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.targetOverride"), Options.bossesVisibilityOverride)
                    .setDefaultValue(false)
                    .setTooltip(Text.translatable("tooltip.provihealth.targetOverride"))
                    .setSaveConsumer(newValue -> Options.bossesVisibilityOverride = newValue)
                    .build()
            ))
            .build()
        );

        health.addEntry(entryBuilder.startSubCategory(Text.translatable("subcategory.provihealth.hostile"), Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.healthBar"), VisibilityType.class, Options.hostile)
                    .setDefaultValue(VisibilityType.ALWAYS_SHOW)
                    .setSaveConsumer(newValue -> Options.hostile = newValue)
                    .build(),
                entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.targetOverride"), Options.hostileVisibilityOverride)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.provihealth.targetOverride"))
                    .setSaveConsumer(newValue -> Options.hostileVisibilityOverride = newValue)
                    .build()
            ))
            .build()
        );

        health.addEntry(entryBuilder.startSubCategory(Text.translatable("subcategory.provihealth.player"), Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.healthBar"), VisibilityType.class, Options.players)
                    .setDefaultValue(VisibilityType.HIDE_IF_FULL)
                    .setSaveConsumer(newValue -> Options.players = newValue)
                    .build(),
                entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.targetOverride"), Options.playersVisibilityOverride)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.provihealth.targetOverride"))
                    .setSaveConsumer(newValue -> Options.playersVisibilityOverride = newValue)
                    .build()
            ))
            .build()
        );

        health.addEntry(entryBuilder.startSubCategory(Text.translatable("subcategory.provihealth.other"), Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.healthBar"), VisibilityType.class, Options.others)
                    .setDefaultValue(VisibilityType.HIDE_IF_FULL)
                    .setSaveConsumer(newValue -> Options.others = newValue)
                    .build(),
                entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.targetOverride"), Options.othersVisibilityOverride)
                    .setDefaultValue(true)
                    .setTooltip(Text.translatable("tooltip.provihealth.targetOverride"))
                    .setSaveConsumer(newValue -> Options.othersVisibilityOverride = newValue)
                    .build()
            ))
            .build()
        );

        health.addEntry(entryBuilder.startStrList(Text.translatable("entry.provihealth.blacklist"), Options.blacklist)
            .setDefaultValue(Arrays.asList("minecraft:armor_stand"))
            .setSaveConsumer(newValue -> Options.blacklist = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.damageParticles"), Options.spawnDamageParticles)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> Options.spawnDamageParticles = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.healingParticles"), Options.spawnHealingParticles)
            .setDefaultValue(false)
            .setSaveConsumer(newValue -> Options.spawnHealingParticles = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.particleType"), DamageParticleType.class, Options.particleType)
            .setDefaultValue(DamageParticleType.RISING)
            .setSaveConsumer(newValue -> Options.particleType = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.damageColour"), Options.damageColour)
            .setDefaultValue(0xFF0000)
            .setSaveConsumer(newValue -> {
                Options.damageColour = newValue;
                Options.unpackedDamage = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        particles.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.damageAlpha"), Options.damageAlpha)
            .setDefaultValue(1f)
            .setMin(0f)
            .setMax(1f)
            .setSaveConsumer(newValue -> Options.damageAlpha = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.healingColour"), Options.healingColour)
            .setDefaultValue(0x00FF00)
            .setSaveConsumer(newValue -> {
                Options.healingColour = newValue;
                Options.unpackedHealing = new Vec3f(Vec3d.unpackRgb(newValue));
            })
            .build()
        );

        particles.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.healingAlpha"), Options.healingAlpha)
            .setDefaultValue(1f)
            .setMin(0f)
            .setMax(1f)
            .setSaveConsumer(newValue -> Options.healingAlpha = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.damageParticleTextColour"), Options.damageParticleTextColour)
            .setDefaultValue(0xFFFFFF)
            .setSaveConsumer(newValue -> Options.damageParticleTextColour = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startColorField(Text.translatable("entry.provihealth.healingParticleTextColour"), Options.healingParticleTextColour)
            .setDefaultValue(0xFFFFFF)
            .setSaveConsumer(newValue -> Options.healingParticleTextColour = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.particleTextShadow"), Options.particleTextShadow)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> Options.particleTextShadow = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.particleScale"), Options.particleScale)
            .setDefaultValue(0.25f)
            .setMin(0.01f)
            .setSaveConsumer(newValue -> Options.particleScale = newValue)
            .build()
        );

        particles.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.maxParticleDistance"), Options.maxParticleDistance)
            .setDefaultValue(16f)
            .setMin(0f)
            .setSaveConsumer(newValue -> Options.maxParticleDistance = newValue)
            .build()
        );

        compatibility.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.compatText"), SeeThroughText.class, Options.seeThroughTextType)
            .setDefaultValue(SeeThroughText.STANDARD)
            .setTooltip(Text.translatable("tooltip.provihealth.compatText"))
            .setSaveConsumer(newValue -> Options.seeThroughTextType = newValue)
            .build()
        );

        compatibility.addEntry(entryBuilder.startBooleanToggle(Text.translatable("entry.provihealth.compatWorld"), Options.compatInWorld)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("tooltip.provihealth.compatWorld"))
            .setSaveConsumer(newValue -> Options.compatInWorld = newValue)
            .build()
        );

        compatibility.addEntry(entryBuilder.startEnumSelector(Text.translatable("entry.provihealth.compatHud"), HUDPortraitCompatMode.class, Options.HUDCompat)
            .setDefaultValue(HUDPortraitCompatMode.STANDARD)
            .setSaveConsumer(newValue -> Options.HUDCompat = newValue)
            .build()
        );

        builder.setSavingRunnable(Options::save);
        return builder.build();
    }
}
