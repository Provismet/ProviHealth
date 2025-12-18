package com.provismet.provihealth.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.provismet.lilylib.util.json.JsonBuilder;
import com.provismet.lilylib.util.json.JsonReader;
import com.provismet.provihealth.ProviHealthClient;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Options {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("provihealth.json");

    public static int maxHealthBarTicks = 40;

    public static List<String> blacklist = List.of("minecraft:armor_stand");
    public static List<String> blacklistHUD = List.of("minecraft:armor_stand");

    public static VisibilityType bosses = VisibilityType.ALWAYS_HIDE;
    public static VisibilityType hostile = VisibilityType.ALWAYS_SHOW;
    public static VisibilityType players = VisibilityType.HIDE_IF_FULL;
    public static VisibilityType others = VisibilityType.HIDE_IF_FULL;

    public static boolean bossesVisibilityOverride = false;
    public static boolean hostileVisibilityOverride = true;
    public static boolean playersVisibilityOverride = true;
    public static boolean othersVisibilityOverride = true;

    public static HUDType bossHUD = HUDType.FULL;
    public static HUDType hostileHUD = HUDType.FULL;
    public static HUDType playerHUD = HUDType.FULL;
    public static HUDType otherHUD = HUDType.FULL;

    public static float hudGlide = 0.5f;

    public static boolean showHudIcon = true;
    public static boolean useCustomHudPortraits = true;
    public static int hudOffsetPercent = 0;
    public static HUDPosition hudPosition = HUDPosition.LEFT;
    public static int hudStartColour = 0x00C100;
    public static int hudEndColour = 0xFF0000;
    public static boolean hudGradient = false;
    public static boolean hudTitles = true;
    public static boolean hudStatuses = true;

    public static boolean showTextInWorld = true;
    public static float maxRenderDistance = 24f;
    public static float worldHealthBarScale = 1.5f;
    public static int worldStartColour = 0x00C100;
    public static int worldEndColour = 0xFF0000;
    public static boolean worldGradient = false;
    public static boolean overrideLabels = false;
    public static boolean worldShadows = true;
    public static float worldOffsetY = 0f;
    public static boolean worldTitles = true;
    public static boolean tintBackground = false;
    public static boolean useTeamColours = false;

    public static boolean spawnDamageParticles = true;
    public static boolean spawnHealingParticles = false;
    public static int damageColour = 0xFF0000;
    public static int healingColour = 0x00FF00;
    public static float particleScale = 0.25f;
    public static boolean particleTextShadow = true;
    public static int damageParticleTextColour = 0xFFFFFFFF;
    public static int healingParticleTextColour = 0xFFFFFFFF;
    public static DamageParticleType particleType = DamageParticleType.RISING;
    public static float maxParticleDistance = 16f;
    public static float damageAlpha = 1f;
    public static float healingAlpha = 1f;

    public static SeeThroughText seeThroughTextType = SeeThroughText.STANDARD;
    public static HUDPortraitCompatMode HUDCompat = HUDPortraitCompatMode.STANDARD;

    public static boolean shouldRenderHealthFor (LivingEntity livingEntity) {
        if (blacklist.contains(EntityType.getId(livingEntity.getType()).toString())) return false;
//        float maxDistance = RenderSystem.getShaderFog().length();
//        if (maxDistance < 1) maxDistance = Options.maxRenderDistance;
//        if (livingEntity.distanceTo(MinecraftClient.getInstance().player) > Math.min(Options.maxRenderDistance, maxDistance)) return false;

        Entity target = MinecraftClient.getInstance().targetedEntity;
        if (livingEntity.getType().isIn(ConventionalEntityTypeTags.BOSSES)) {
            if (bossesVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(bosses, livingEntity);
        }
        else if (livingEntity instanceof HostileEntity) {
            if (hostileVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(hostile, livingEntity);
        }
        else if (livingEntity instanceof PlayerEntity) {
            if (playersVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(players, livingEntity);
        }
        else {
            if (othersVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(others, livingEntity);
        }
    }

    public static boolean isBlacklisted (Entity entity, @Nullable BarType barType) {
        return switch (barType) {
            case null -> false;
            case WORLD -> Options.blacklist.contains(EntityType.getId(entity.getType()).toString());
            case HUD -> Options.blacklistHUD.contains(EntityType.getId(entity.getType()).toString());
        };
    }

    public static void save () {
        String jsonData = new JsonBuilder()
            .append("hud", new JsonBuilder()
                .append("hudDuration", maxHealthBarTicks)
                .append("hudIcon", showHudIcon)
                .append("hudPortraits", useCustomHudPortraits)
                .append("hudGlide", hudGlide)
                .append("hudPosition", hudPosition.name())
                .append("hudOffsetY", hudOffsetPercent)
                .append("hudGradient", hudGradient)
                .append("hudStartColour", hudStartColour)
                .append("hudEndColour", hudEndColour)
                .append("bossHUD", bossHUD.name())
                .append("hostileHUD", hostileHUD.name())
                .append("playerHUD", playerHUD.name())
                .append("otherHUD", otherHUD.name())
                .append("hudTitles", hudTitles)
                .append("hudStatusEffects", hudStatuses)
                .appendArray("hudBlacklist", blacklistHUD)
            )
            .append("world", new JsonBuilder()
                .append("replaceLabels", overrideLabels)
                .append("worldHealthText", showTextInWorld)
                .append("worldTextShadows", worldShadows)
                .append("maxRenderDistance", maxRenderDistance)
                .append("barScale", worldHealthBarScale)
                .append("worldOffsetY", worldOffsetY)
                .append("worldGradient", worldGradient)
                .append("worldStartColour", worldStartColour)
                .append("worldEndColour", worldEndColour)
                .append("bossHealth", bosses.name())
                .append("bossTarget", bossesVisibilityOverride)
                .append("hostileHealth", hostile.name())
                .append("hostileTarget", hostileVisibilityOverride)
                .append("playerHealth", players.name())
                .append("playerTarget", playersVisibilityOverride)
                .append("otherHealth", others.name())
                .append("otherTarget", othersVisibilityOverride)
                .append("worldTitles", worldTitles)
                .append("tintBackground", tintBackground)
                .append("useTeamColours", useTeamColours)
                .appendArray("healthBlacklist", blacklist)
            )
            .append("particles", new JsonBuilder()
                .append("damageParticles", spawnDamageParticles)
                .append("healingParticles", spawnHealingParticles)
                .append("damageColour", damageColour)
                .append("damageAlpha", damageAlpha)
                .append("healingColour", healingColour)
                .append("healingAlpha", healingAlpha)
                .append("particleScale", particleScale)
                .append("particleTextShadow", particleTextShadow)
                .append("damageParticleTextColour", damageParticleTextColour)
                .append("healingParticleTextColour", healingParticleTextColour)
                .append("particleType", particleType.name())
                .append("maxParticleDistance", maxParticleDistance)
            )
            .append("compatibility", new JsonBuilder()
                .append("topLayerTextType", seeThroughTextType.name())
                .append("compatHudPaperdoll", HUDCompat.name())
            )
            .toString();

        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            writer.write(jsonData);
        }
        catch (IOException e) {
            ProviHealthClient.LOGGER.error("Error whilst saving config: ", e);
        }
    }

    public static void load () {
        try {
            JsonReader jsonReader = JsonReader.file(FILE.toFile());
            if (jsonReader == null) {
                save();
                return;
            }

            jsonReader.get("hud").map(element -> {
                if (element instanceof JsonObject jsonObject) return new JsonReader(jsonObject);
                else return null;
            }).ifPresent(json -> {
                json.getInteger("hudDuration").ifPresent(val -> maxHealthBarTicks = val);
                json.getBoolean("hudIcon").ifPresent(val -> showHudIcon = val);
                json.getBoolean("hudPortraits").ifPresent(val -> useCustomHudPortraits = val);
                json.getFloat("hudGlide").ifPresent(val -> hudGlide = val);
                json.getString("hudPosition").ifPresent(val -> hudPosition = HUDPosition.valueOf(val));
                json.getInteger("hudOffsetY").ifPresent(val -> hudOffsetPercent = val);
                json.getBoolean("hudGradient").ifPresent(val -> hudGradient = val);
                json.getInteger("hudStartColour").ifPresent(val -> hudStartColour = val);
                json.getInteger("hudEndColour").ifPresent(val -> hudEndColour = val);
                json.getString("bossHUD").ifPresent(val -> bossHUD = HUDType.valueOf(val));
                json.getString("hostileHUD").ifPresent(val -> hostileHUD = HUDType.valueOf(val));
                json.getString("playerHUD").ifPresent(val -> playerHUD = HUDType.valueOf(val));
                json.getString("otherHUD").ifPresent(val -> otherHUD = HUDType.valueOf(val));
                json.getBoolean("hudTitles").ifPresent(val -> hudTitles = val);
                json.getBoolean("hudStatusEffects").ifPresent(val -> hudStatuses = val);
                json.getArray("hudBlacklist").ifPresent(val -> blacklistHUD = val.asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList());
            });

            jsonReader.get("world").map(element -> {
                if (element instanceof JsonObject jsonObject) return new JsonReader(jsonObject);
                else return null;
            }).ifPresent(json -> {
                json.getBoolean("replaceLabels").ifPresent(val -> overrideLabels = val);
                json.getBoolean("worldHealthText").ifPresent(val -> showTextInWorld = val);
                json.getBoolean("worldTextShadows").ifPresent(val -> worldShadows = val);
                json.getFloat("maxRenderDistance").ifPresent(val -> maxRenderDistance = val);
                json.getFloat("barScale").ifPresent(val -> worldHealthBarScale = val);
                json.getFloat("worldOffsetY").ifPresent(val -> worldOffsetY = val);
                json.getBoolean("worldGradient").ifPresent(val -> worldGradient = val);
                json.getInteger("worldStartColour").ifPresent(val -> worldStartColour = val);
                json.getInteger("worldEndColour").ifPresent(val -> worldEndColour = val);
                json.getString("bossHealth").ifPresent(val -> bosses = VisibilityType.valueOf(val));
                json.getBoolean("bossTarget").ifPresent(val -> bossesVisibilityOverride = val);
                json.getString("hostileHealth").ifPresent(val -> hostile = VisibilityType.valueOf(val));
                json.getBoolean("hostileTarget").ifPresent(val -> hostileVisibilityOverride = val);
                json.getString("playerHealth").ifPresent(val -> players = VisibilityType.valueOf(val));
                json.getBoolean("playerTarget").ifPresent(val -> playersVisibilityOverride = val);
                json.getString("otherHealth").ifPresent(val -> others = VisibilityType.valueOf(val));
                json.getBoolean("otherTarget").ifPresent(val -> othersVisibilityOverride = val);
                json.getBoolean("worldTitles").ifPresent(val -> worldTitles = val);
                json.getBoolean("tintBackground").ifPresent(val -> tintBackground = val);
                json.getBoolean("useTeamColours").ifPresent(val -> useTeamColours = val);
                json.getArray("healthBlacklist").ifPresent(val -> blacklist = val.asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList());
            });

            jsonReader.get("particles").map(element -> {
                if (element instanceof JsonObject jsonObject) return new JsonReader(jsonObject);
                else return null;
            }).ifPresent(json -> {
                json.getBoolean("damageParticles").ifPresent(val -> spawnDamageParticles = val);
                json.getBoolean("healingParticles").ifPresent(val -> spawnHealingParticles = val);
                json.getInteger("damageColour").ifPresent(val -> damageColour = val);
                json.getFloat("damageAlpha").ifPresent(val -> damageAlpha = val);
                json.getInteger("healingColour").ifPresent(val -> healingColour = val);
                json.getFloat("healingAlpha").ifPresent(val -> healingAlpha = val);
                json.getFloat("particleScale").ifPresent(val -> particleScale = val);
                json.getBoolean("particleTextShadow").ifPresent(val -> particleTextShadow = val);
                json.getInteger("damageParticleTextColour").ifPresent(val -> damageParticleTextColour = val);
                json.getInteger("healingParticleTextColour").ifPresent(val -> healingParticleTextColour = val);
                json.getString("particleType").ifPresent(val -> particleType = DamageParticleType.valueOf(val));
                json.getFloat("maxParticleDistance").ifPresent(val -> maxParticleDistance = val);
            });

            jsonReader.get("compatibility").map(element -> {
                if (element instanceof JsonObject jsonObject) return new JsonReader(jsonObject);
                else return null;
            }).ifPresent(json -> {
                json.getString("topLayerTextType").ifPresent(val -> seeThroughTextType = SeeThroughText.valueOf(val));
                json.getString("compatHudPaperdoll").ifPresent(val -> HUDCompat = HUDPortraitCompatMode.valueOf(val));
            });

            // Sanitise the config file.
            save();
        }
        catch (FileNotFoundException e) {
            ProviHealthClient.LOGGER.info("No config found, creating new one.");
            save();
        }
    }

    private static boolean shouldRenderHealthFor (VisibilityType type, LivingEntity livingEntity) {
        return switch (type) {
            case ALWAYS_HIDE -> false;
            case HIDE_IF_FULL -> {
                if (livingEntity.getHealth() < livingEntity.getMaxHealth()) yield true;
                else if (livingEntity.hasVehicle()) {
                    Entity vehicle = livingEntity.getVehicle();
                    while (vehicle != null) {
                        if (vehicle instanceof LivingEntity livingVehicle) {
                            if (livingVehicle.getHealth() < livingVehicle.getMaxHealth()) yield true;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                yield false;
            }
            default -> true;
        };
    }

    public enum BarType {
        WORLD,
        HUD
    }

    public enum VisibilityType {
        ALWAYS_HIDE,
        HIDE_IF_FULL,
        ALWAYS_SHOW;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public enum HUDType {
        NONE(false, false, false),
        PORTRAIT_ONLY(true, false, false),
        FULL(true, true, true);

        public final boolean showPortrait;
        public final boolean showBars;
        public final boolean showTitles;

        HUDType (boolean showPortrait, boolean showBars, boolean showTitles) {
            this.showPortrait = showPortrait;
            this.showBars = showBars;
            this.showTitles = showTitles;
        }

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public enum DamageParticleType {
        RISING,
        GRAVITY,
        STATIC;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public enum HUDPosition {
        LEFT(150f),
        RIGHT(210f);

        public final float portraitYAW;

        HUDPosition(float portraitYAW) {
            this.portraitYAW = portraitYAW;
        }

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public enum SeeThroughText {
        STANDARD,
        NONE,
        FULL;

        @Override
        public String toString () {
            return "enum.provihealth.seethroughtext." + super.toString().toLowerCase();
        }
    }

    public enum HUDPortraitCompatMode {
        STANDARD,
        COMPAT,
        NONE;

        @Override
        public String toString () {
            return "enum.provihealth.hudportraitcompatmode." + super.toString().toLowerCase();
        }
    }
}
