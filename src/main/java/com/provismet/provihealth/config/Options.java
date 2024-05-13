package com.provismet.provihealth.config;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalEntityTypeTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.math.Vec3f;

import com.google.gson.stream.JsonReader;
import com.provismet.provihealth.ProviHealthClient;

public class Options {
    public static final Vec3f WHITE = new Vec3f(Vec3d.unpackRgb(0xFFFFFF));

    public static int maxHealthBarTicks = 40;

    public static List<String> blacklist = Arrays.asList("minecraft:armor_stand");
    public static List<String> blacklistHUD = Arrays.asList("minecraft:armor_stand");

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
    public static float worldGlide = 0.5f;

    public static boolean showHudIcon = true;
    public static boolean useCustomHudPortraits = true;
    public static int hudOffsetPercent = 0;
    public static HUDPosition hudPosition = HUDPosition.LEFT;
    public static int hudStartColour = 0x00C100;
    public static int hudEndColour = 0xFF0000;
    public static Vec3f unpackedStartHud = new Vec3f(Vec3d.unpackRgb(hudStartColour));
    public static Vec3f unpackedEndHud = new Vec3f(Vec3d.unpackRgb(hudEndColour));
    public static boolean hudGradient = false;

    public static boolean showTextInWorld = true;
    public static float maxRenderDistance = 24f;
    public static float worldHealthBarScale = 1.5f;
    public static int worldStartColour = 0x00C100;
    public static int worldEndColour = 0xFF0000;
    public static Vec3f unpackedStartWorld = new Vec3f(Vec3d.unpackRgb(worldStartColour));
    public static Vec3f unpackedEndWorld = new Vec3f(Vec3d.unpackRgb(worldEndColour));
    public static boolean worldGradient = false;
    public static boolean overrideLabels = false;
    public static boolean worldShadows = true;
    public static float worldOffsetY = 0f;

    public static boolean spawnDamageParticles = true;
    public static boolean spawnHealingParticles = false;
    public static int damageColour = 0xFF0000;
    public static int healingColour = 0x00FF00;
    public static Vec3f unpackedDamage = new Vec3f(Vec3d.unpackRgb(damageColour));
    public static Vec3f unpackedHealing = new Vec3f(Vec3d.unpackRgb(healingColour));
    public static float particleScale = 0.25f;
    public static boolean particleTextShadow = true;
    public static int damageParticleTextColour = 0xFFFFFF;
    public static int healingParticleTextColour = 0xFFFFFF;
    public static DamageParticleType particleType = DamageParticleType.RISING;
    public static float maxParticleDistance = 16f;
    public static float damageAlpha = 1f;
    public static float healingAlpha = 1f;

    public static SeeThroughText seeThroughTextType = SeeThroughText.STANDARD;
    public static boolean compatInWorld = false;
    public static boolean compatInHUD = false;
    public static HUDPortraitCompatMode HUDCompat = HUDPortraitCompatMode.STANDARD;

    public static boolean shouldRenderHealthFor (LivingEntity livingEntity) {
        if (blacklist.contains(EntityType.getId(livingEntity.getType()).toString())) return false;
        if (livingEntity.distanceTo(MinecraftClient.getInstance().player) > Options.maxRenderDistance) return false;

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

    public static HUDType getHUDFor (LivingEntity livingEntity) {
        if (blacklistHUD.contains(EntityType.getId(livingEntity.getType()).toString())) return HUDType.NONE;
        else if (livingEntity.getType().isIn(ConventionalEntityTypeTags.BOSSES)) return bossHUD;
        else if (livingEntity instanceof HostileEntity) return hostileHUD;
        else if (livingEntity instanceof PlayerEntity) return playerHUD;
        else return otherHUD;
    }

    public static Vec3f getBarColour (float percentage, Vec3f start, Vec3f end, boolean shouldGradient) {
        if (shouldGradient) {
            return new Vec3f(
                    MathHelper.lerp(percentage, end.getX(), start.getX()),
                    MathHelper.lerp(percentage, end.getY(), start.getY()),
                    MathHelper.lerp(percentage, end.getZ(), start.getZ())
            );
        }
        else return start;
    }

    public static void save () {
        JsonHelper json = new JsonHelper();
        String jsonData = json.start()
            .append("hudDuration", maxHealthBarTicks).newLine()
            .append("hudIcon", showHudIcon).newLine()
            .append("hudPortraits", useCustomHudPortraits).newLine()
            .append("hudGlide", hudGlide).newLine()
            .append("hudPosition", hudPosition.name()).newLine()
            .append("hudOffsetY", hudOffsetPercent).newLine()
            .append("hudGradient", hudGradient).newLine()
            .append("hudStartColour", hudStartColour).newLine()
            .append("hudEndColour", hudEndColour).newLine()
            .append("replaceLabels", overrideLabels).newLine()
            .append("worldGlide", worldGlide).newLine()
            .append("worldHealthText", showTextInWorld).newLine()
            .append("worldTextShadows", worldShadows).newLine()
            .append("maxRenderDistance", maxRenderDistance).newLine()
            .append("barScale", worldHealthBarScale).newLine()
            .append("worldOffsetY", worldOffsetY).newLine()
            .append("worldGradient", worldGradient).newLine()
            .append("worldStartColour", worldStartColour).newLine()
            .append("worldEndColour", worldEndColour).newLine()
            .append("bossHealth", bosses.name()).newLine()
            .append("bossTarget", bossesVisibilityOverride).newLine()
            .append("hostileHealth", hostile.name()).newLine()
            .append("hostileTarget", hostileVisibilityOverride).newLine()
            .append("playerHealth", players.name()).newLine()
            .append("playerTarget", playersVisibilityOverride).newLine()
            .append("otherHealth", others.name()).newLine()
            .append("otherTarget", othersVisibilityOverride).newLine()
            .append("bossHUD", bossHUD.name()).newLine()
            .append("hostileHUD", hostileHUD.name()).newLine()
            .append("playerHUD", playerHUD.name()).newLine()
            .append("otherHUD", otherHUD.name()).newLine()
            .append("damageParticles", spawnDamageParticles).newLine()
            .append("healingParticles", spawnHealingParticles).newLine()
            .append("damageColour", damageColour).newLine()
            .append("damageAlpha", damageAlpha).newLine()
            .append("healingColour", healingColour).newLine()
            .append("healingAlpha", healingAlpha).newLine()
            .append("particleScale", particleScale).newLine()
            .append("particleTextShadow", particleTextShadow).newLine()
            .append("damageParticleTextColour", damageParticleTextColour).newLine()
            .append("healingParticleTextColour", healingParticleTextColour).newLine()
            .append("particleType", particleType.name()).newLine()
            .append("maxParticleDistance", maxParticleDistance).newLine()
            .append("topLayerTextType", seeThroughTextType.name()).newLine()
            .append("compatWorldBar", compatInWorld).newLine()
            .append("compatHudPaperdoll", HUDCompat.name()).newLine()
            .createArray("healthBlacklist", blacklist).newLine()
            .createArray("hudBlacklist", blacklistHUD).newLine(false)
            .closeObject()
            .toString();

        try {
            FileWriter writer = new FileWriter("config/provihealth.json");
            writer.write(jsonData);
            writer.close();
        }
        catch (IOException e) {
            ProviHealthClient.LOGGER.error("Error whilst saving config: ", e);
        }
    }

    public static void load () {
        try {
            FileReader reader = new FileReader("config/provihealth.json");
            JsonReader parser = new JsonReader(reader);

            parser.beginObject();
            while (parser.hasNext()) {
                final String label = parser.nextName();

                switch (label) {
                    case "hudDuration":
                        maxHealthBarTicks = parser.nextInt();
                        break;
                    
                    case "hudIcon":
                        showHudIcon = parser.nextBoolean();
                        break;
                    
                    case "hudPortraits":
                        useCustomHudPortraits = parser.nextBoolean();
                        break;
                    
                    case "hudGlide":
                        hudGlide = (float)parser.nextDouble();
                        break;
                    
                    case "hudPosition":
                        hudPosition = HUDPosition.valueOf(parser.nextString());
                        break;

                    case "hudOffsetY":
                        hudOffsetPercent = parser.nextInt();
                        break;

                    case "hudGradient":
                        hudGradient = parser.nextBoolean();
                        break;
                    
                    case "hudStartColour":
                        hudStartColour = parser.nextInt();
                        unpackedStartHud = new Vec3f(Vec3d.unpackRgb(hudStartColour));
                        break;

                    case "hudEndColour":
                        hudEndColour = parser.nextInt();
                        unpackedEndHud = new Vec3f(Vec3d.unpackRgb(hudEndColour));
                        break;

                    case "replaceLabels":
                        overrideLabels = parser.nextBoolean();
                        break;
                    
                    case "worldGlide":
                        worldGlide = (float)parser.nextDouble();
                        break;

                    case "worldHealthText":
                        showTextInWorld = parser.nextBoolean();
                        break;

                    case "worldTextShadows":
                        worldShadows = parser.nextBoolean();
                        break;
                    
                    case "maxRenderDistance":
                        maxRenderDistance = (float)parser.nextDouble();
                        break;

                    case "barScale":
                        worldHealthBarScale = (float)parser.nextDouble();
                        break;

                    case "worldOffsetY":
                        worldOffsetY = (float)parser.nextDouble();
                        break;

                    case "worldGradient":
                        worldGradient = parser.nextBoolean();
                        break;

                    case "worldStartColour":
                        worldStartColour = parser.nextInt();
                        unpackedStartWorld = new Vec3f(Vec3d.unpackRgb(worldStartColour));
                        break;

                    case "worldEndColour":
                        worldEndColour = parser.nextInt();
                        unpackedEndWorld = new Vec3f(Vec3d.unpackRgb(worldEndColour));
                        break;

                    case "bossHealth":
                        bosses = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "bossTarget":
                        bossesVisibilityOverride = parser.nextBoolean();
                        break;

                    case "bossHUD":
                        bossHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "hostileHealth":
                        hostile = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "hostileTarget":
                        hostileVisibilityOverride = parser.nextBoolean();
                        break;
                    
                    case "hostileHUD":
                        hostileHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "playerHealth":
                        players = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "playerTarget":
                        playersVisibilityOverride = parser.nextBoolean();
                        break;

                    case "playerHUD":
                        playerHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "otherHealth":
                        others = VisibilityType.valueOf(parser.nextString());
                        break;

                    case "otherTarget":
                        othersVisibilityOverride = parser.nextBoolean();
                        break;

                    case "otherHUD":
                        otherHUD = HUDType.valueOf(parser.nextString());
                        break;

                    case "damageParticles":
                        spawnDamageParticles = parser.nextBoolean();
                        break;

                    case "healingParticles":
                        spawnHealingParticles = parser.nextBoolean();
                        break;

                    case "damageColour":
                        damageColour = parser.nextInt();
                        unpackedDamage = new Vec3f(Vec3d.unpackRgb(damageColour));
                        break;

                    case "damageAlpha":
                        damageAlpha = (float)parser.nextDouble();
                        break;

                    case "healingColour":
                        healingColour = parser.nextInt();
                        unpackedHealing = new Vec3f(Vec3d.unpackRgb(healingColour));
                        break;

                    case "healingAlpha":
                        healingAlpha = (float)parser.nextDouble();
                        break;

                    case "particleScale":
                        particleScale = (float)parser.nextDouble();
                        break;

                    case "particleTextShadow":
                        particleTextShadow = parser.nextBoolean();
                        break;

                    case "damageParticleTextColour":
                        damageParticleTextColour = parser.nextInt();
                        break;

                    case "healingParticleTextColour":
                        healingParticleTextColour = parser.nextInt();
                        break;

                    case "particleType":
                        particleType = DamageParticleType.valueOf(parser.nextString());
                        break;

                    case "maxParticleDistance":
                        maxParticleDistance = (float)parser.nextDouble();
                        break;

                    case "topLayerTextType":
                        seeThroughTextType = SeeThroughText.valueOf(parser.nextString());
                        break;

                    case "compatWorldBar":
                        compatInWorld = parser.nextBoolean();
                        break;

                    case "compatHudPaperdoll":
                        HUDCompat = HUDPortraitCompatMode.valueOf(parser.nextString());
                        break;

                    case "healthBlacklist":
                        ArrayList<String> tempBlacklist = new ArrayList<>();
                        parser.beginArray();
                        while (parser.hasNext()) {
                            tempBlacklist.add(parser.nextString());
                        }
                        parser.endArray();
                        blacklist = tempBlacklist;
                        break;

                    case "hudBlacklist":
                        ArrayList<String> tempHudBlacklist = new ArrayList<>();
                        parser.beginArray();
                        while (parser.hasNext()) {
                            tempHudBlacklist.add(parser.nextString());
                        }
                        parser.endArray();
                        blacklistHUD = tempHudBlacklist;
                        break;
                
                    default:
                        ProviHealthClient.LOGGER.warn("Unknown label \"" + label + "\" found in config.");
                        parser.skipValue();
                        break;
                }
            }
            parser.close();
        }
        catch (FileNotFoundException e) {
            ProviHealthClient.LOGGER.info("No config found, creating new one.");
            save();
        }
        catch (IOException e2) {
            ProviHealthClient.LOGGER.error("Error whilst parsing config: ", e2);
        }
    }

    private static boolean shouldRenderHealthFor (VisibilityType type, LivingEntity livingEntity) {
        switch (type) {
            case ALWAYS_HIDE:
                return false;
        
            case HIDE_IF_FULL:
                if (livingEntity.getHealth() < livingEntity.getMaxHealth()) return true;
                else if (livingEntity.hasVehicle()) {
                    Entity vehicle = livingEntity.getVehicle();
                    while (vehicle != null) {
                        if (vehicle instanceof LivingEntity livingVehicle) {
                            if (livingVehicle.getHealth() < livingVehicle.getMaxHealth()) return true;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                return false;
            
            case ALWAYS_SHOW:
            default:
                return true;
        }
    }

    public static enum VisibilityType {
        ALWAYS_HIDE,
        HIDE_IF_FULL,
        ALWAYS_SHOW;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public static enum HUDType {
        NONE,
        PORTRAIT_ONLY,
        FULL;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public static enum DamageParticleType {
        RISING,
        GRAVITY,
        STATIC;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public static enum HUDPosition {
        LEFT(150f),
        RIGHT(210f);

        public final float portraitYAW;

        private HUDPosition (float portraitYAW) {
            this.portraitYAW = portraitYAW;
        }

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public static enum SeeThroughText {
        STANDARD,
        NONE,
        FULL;

        @Override
        public String toString () {
            return "enum.provihealth.seethroughtext." + super.toString().toLowerCase();
        }
    }

    public static enum HUDPortraitCompatMode {
        STANDARD,
        COMPAT,
        NONE;

        @Override
        public String toString () {
            return "enum.provihealth.hudportraitcompatmode." + super.toString().toLowerCase();
        }
    }
}
