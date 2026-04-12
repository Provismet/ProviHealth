package com.provismet.provihealth.config.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.ElementRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class EntityOptions {
    public static final EntityOptions DEFAULT = new EntityOptions(null, null, null, null);

    public static final Codec<EntityOptions> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("border").forGetter(options -> Optional.ofNullable(options.border)),
            Identifier.CODEC.optionalFieldOf("healthbar").forGetter(options -> Optional.ofNullable(options.healthBar)),
            ItemStack.CODEC.optionalFieldOf("icon").forGetter(options -> Optional.ofNullable(options.icon)),
            ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("hudType").forGetter(options -> Optional.ofNullable(options.hudType).map(Enum::name))
        ).apply(instance, (border, health, icon, hud) -> new EntityOptions(border.orElse(null), health.orElse(null), icon.orElse(null), hud.orElse(null)))
    );

    private final Identifier border;
    private final Identifier healthBar;
    private final ItemStack icon;
    private final Options.HUDType hudType;

    public EntityOptions (Identifier border, Identifier healthBar, ItemStack icon, String hudType) {
        this.border = border;
        this.healthBar = healthBar;
        this.icon = icon;
        if (hudType == null) this.hudType = null;
        else this.hudType = Options.HUDType.valueOf(hudType);
    }

    @NotNull
    public Identifier getBorder (LivingEntity entity) {
        if (this.border == null) return Objects.requireNonNullElse(ElementRegistry.getOrCacheBorder(entity), ElementRegistry.DEFAULT_BORDER);
        if (!Options.useCustomHudPortraits) return ElementRegistry.DEFAULT_BORDER;
        return this.border;
    }

    @NotNull
    public Identifier getHealthBar (LivingEntity entity) {
        if (this.healthBar == null) return Objects.requireNonNullElse(ElementRegistry.getOrCacheHealthBar(entity), ElementRegistry.DEFAULT_BARS);
        return this.healthBar;
    }

    @Nullable
    public ItemStack getIcon (LivingEntity entity) {
        if (this.icon == null) return ElementRegistry.getOrCacheIcon(entity);
        return this.icon;
    }

    @NotNull
    public Options.HUDType getHudType (LivingEntity entity) {
        if (Options.isBlacklisted(entity, Options.BarType.HUD)) return Options.HUDType.NONE;
        if (this.hudType == null) {
            Options.HUDType fromCache = ElementRegistry.getOrCacheHudType(entity);
            if (fromCache != null) return fromCache;

            if (entity.getType().is(ConventionalEntityTypeTags.BOSSES)) return Options.bossHUD;
            else if (entity instanceof Monster) return Options.hostileHUD;
            else if (entity instanceof Player) return Options.playerHUD;
            else return Options.otherHUD;
        }
        return this.hudType;
    }
}
