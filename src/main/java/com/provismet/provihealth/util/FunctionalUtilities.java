package com.provismet.provihealth.util;

import org.joml.Vector3f;

import com.provismet.provihealth.config.Options;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.TameableEntity;

public class FunctionalUtilities {
    public static Vector3f deduceColour (LivingEntity entity, boolean inHud) {

        // ? Llamas are neutral in behaviour but do not extend Angerable, so they need their own checks
        // ? If a neutral mob is angered, the bar changes colours to hostile colours.
        // ? Neutrality is checked first, because Endermen, Zombie Piglins, and other Angerable AND HostileEntity mobs should be checked here
        // ? before hostility, as they are not hostile unless provoked.
        // ? Tameable Angerable mobs only appear with neutral colours if they are untamed. Once tamed they are passive

        // ~ Neutral Aggression Level
        if (Options.useHudAggressionColours && (
            // * If the target is a *non-angered* but angerable tameable mob and is not tamed
            (entity instanceof Angerable && entity instanceof TameableEntity && !(((TameableEntity)(entity)).isTamed() && !((Angerable)entity).hasAngerTime()))
            // * If the target is a LlamaEntity
            || (entity instanceof LlamaEntity)
            // * If the target is a *non-angered* Enderman - Endermen have their own Anger TrackedData
            || (entity instanceof EndermanEntity && !((EndermanEntity)entity).isAngry())
            // * If the target is a *non-angered* PiglinEntity (these entities are not Angerable, only HostileEntities)
            || (entity instanceof PiglinEntity && !((MobEntity)entity).isAttacking())
            // * If the target is a generic *non-angered* but angerable untameable mob (not an Enderman)
            || (entity instanceof Angerable && !(entity instanceof TameableEntity) && !(entity instanceof EndermanEntity) && !((Angerable)entity).hasAngerTime() && !((MobEntity)entity).isAttacking())
        ))  {
            if (inHud) return Options.unpackedNeutralStartHud;
            else return Options.unpackedNeutralStartWorld;
        }

        // ~ Aggressive/Hostile Aggression Level
        else if (Options.useWorldAggressionColours && (
            // * If the target is a Hostile Entity, or a Hoglin (which are passive in the code)
            (entity instanceof HostileEntity || entity instanceof HoglinEntity)
            // * If the target is an angered tameable mob and is not tamed
            || (entity instanceof Angerable && entity instanceof TameableEntity && !(((TameableEntity)(entity)).isTamed() && ((Angerable)entity).hasAngerTime()))
            // * If the target is an angered Enderman
            || (entity instanceof EndermanEntity && ((EndermanEntity)entity).isAngry())
            // * If the target is a generic angered untameable mob
            || (entity instanceof Angerable && !(entity instanceof TameableEntity) && (((Angerable)entity).hasAngerTime() || ((MobEntity)entity).isAttacking()))

        ))  {
            if (inHud) return Options.unpackedHostileStartHud;
            else return Options.unpackedHostileStartWorld;
        }

        // ~ Passive and Default Aggression Level (all passive mobs and modded mobs that do not extend vanilla classes)
        if (inHud) return Options.unpackedDefaultStartHud;
        else return Options.unpackedDefaultStartWorld;
    }

    
}
