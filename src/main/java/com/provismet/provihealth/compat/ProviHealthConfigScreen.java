package com.provismet.provihealth.compat;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.HUDType;
import com.provismet.provihealth.config.Options.VisibilityType;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        hud.addEntry(entryBuilder.startIntField(Text.translatable("entry.provihealth.hudDuration"), Options.maxHealthBarTicks)
            .setDefaultValue(40)
            .setMin(0)
            .setTooltip(Text.translatable("tooltip.provihealth.hudDuration"))
            .setSaveConsumer(newValue -> Options.maxHealthBarTicks = newValue)
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

        health.addEntry(entryBuilder.startFloatField(Text.translatable("entry.provihealth.hudGlide"), Options.worldGlide)
            .setDefaultValue(0.5f)
            .setMin(0.01f)
            .setMax(1f)
            .setTooltip(Text.translatable("tooltip.provihealth.glide"))
            .setSaveConsumer(newValue -> Options.worldGlide = newValue)
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

        builder.setSavingRunnable(Options::save);
        return builder.build();
    }
}
