package com.provismet.provihealth.config.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.provismet.provihealth.config.Options;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class TagOptions {
    public static final Codec<TagOptions> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("priority").forGetter(TagOptions::getPriority),
            Identifier.CODEC.optionalFieldOf("border").forGetter(options -> Optional.ofNullable(options.border)),
            Identifier.CODEC.optionalFieldOf("healthbar").forGetter(options -> Optional.ofNullable(options.healthBar)),
            ItemStackTemplate.CODEC.optionalFieldOf("icon").forGetter(options -> Optional.ofNullable(options.icon)),
            ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("hudType").forGetter(options -> Optional.ofNullable(options.hudType).map(Enum::name))
        ).apply(instance, (priority, border, health, icon, hud) -> new TagOptions(
            priority,
            border.orElse(null),
            health.orElse(null),
            icon.orElse(null),
            hud.orElse(null)
        ))
    );

    private final int priority;
    private final Identifier border;
    private final Identifier healthBar;
    private final ItemStackTemplate icon;
    private final Options.HUDType hudType;

    public TagOptions (int priority, Identifier border, Identifier healthBar, ItemStackTemplate icon, String hudType) {
        this.priority = priority;
        this.border = border;
        this.healthBar = healthBar;
        this.icon = icon;
        if (hudType == null) this.hudType = null;
        else this.hudType = Options.HUDType.valueOf(hudType);
    }

    public TagOptions (int priority, Identifier border, Identifier healthBar, Item icon, String hudType) {
        this(priority, border, healthBar, new ItemStackTemplate(icon), hudType);
    }

    public int getPriority () {
        return this.priority;
    }

    @Nullable
    public Identifier getBorder () {
        return this.border;
    }

    @Nullable
    public Identifier getHealthBar () {
        return this.healthBar;
    }

    @Nullable
    public ItemStackTemplate getIcon () {
        return this.icon;
    }

    @Nullable
    public Options.HUDType getHudType () {
        return this.hudType;
    }
}
