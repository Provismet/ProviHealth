package com.provismet.provihealth.mixin;

import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.util.HealthContainer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements IMixinEntityRenderState {
    @Unique
    private boolean shouldRenderHealth;

    @Unique
    private HealthContainer healthContainer;

    @Unique
    private HealthContainer mountHealthContainer;

    @Unique
    private boolean isLiving;

    @Unique
    private List<Text> titles = List.of();

    @Unique
    private boolean shouldRenderLabel;

    @Unique
    private Text healthBarLabel;

    @Override
    public void provi_Health$setShouldRenderHealth (boolean value) {
        this.shouldRenderHealth = value;
    }

    @Override
    public void provi_Health$setHealth (HealthContainer container) {
        this.healthContainer = container;
    }

    @Override
    public void provi_Health$setMountHealth (HealthContainer container) {
        this.mountHealthContainer = container;
    }

    @Override
    public void provi_Health$setIsLiving (boolean value) {
        this.isLiving = true;
    }

    @Override
    public void provi_Health$setTitles (List<Text> titles) {
        this.titles = titles;
    }

    @Override
    public void provi_Health$setShouldRenderLabel (boolean value) {
        this.shouldRenderLabel = value;
    }

    @Override
    public void provi_Health$setLabel (Text label) {
        this.healthBarLabel = label.copy();
    }

    @Override
    public boolean provi_Health$shouldRenderHealth () {
        return this.shouldRenderHealth;
    }

    @Override
    public HealthContainer provi_Health$getHealth () {
        return this.healthContainer;
    }

    @Override
    public HealthContainer provi_Health$getMountHealth () {
        return this.mountHealthContainer;
    }

    @Override
    public boolean provi_Health$isLiving () {
        return this.isLiving;
    }

    @Override
    public List<Text> provi_Health$getTitles () {
        return this.titles;
    }

    @Override
    public boolean provi_Health$shouldRenderLabel () {
        return this.shouldRenderLabel;
    }

    @Override
    public Text provi_Health$getLabel () {
        return this.healthBarLabel;
    }
}
