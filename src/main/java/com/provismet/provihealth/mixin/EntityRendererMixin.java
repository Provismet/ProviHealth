package com.provismet.provihealth.mixin;

import com.provismet.provihealth.hud.BorderRegistry;
import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.util.Visibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.TargetHealthBar;
import com.provismet.provihealth.world.EntityHealthBar;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow @Final
    private TextRenderer textRenderer;

    @Shadow @Final
    protected EntityRenderDispatcher dispatcher;

    @Shadow
    protected abstract boolean hasLabel (Entity entity, double squaredDistanceToCamera);

    @Shadow
    protected abstract @Nullable Text getDisplayName (Entity entity);

    @Inject(method="renderLabelIfPresent", at=@At("HEAD"), cancellable=true)
    private void cancelLabel (EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if (TargetHealthBar.disabledLabels || (Options.overrideLabels &&  ((IMixinEntityRenderState)state).provi_Health$shouldRenderHealth())) info.cancel();
    }

    @Inject(method="render", at=@At("HEAD"))
    private void addHealthBar (EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        EntityHealthBar.render(state, matrices, vertexConsumers, this.dispatcher.getRotation(), this.textRenderer);
    }

    @Inject(method="updateRenderState", at=@At("HEAD"))
    private void modifyRenderState (Entity entity, EntityRenderState state, float tickDelta, CallbackInfo info) {
        if (entity instanceof LivingEntity living) { // Mixin here instead of LivingEntityRenderer because some mods have mobs that bypass it.
            IMixinEntityRenderState mixinState = (IMixinEntityRenderState)state;

            mixinState.provi_Health$setHealth(((IMixinLivingEntity)living).provi_Health$getHealthContainer());
            if (mixinState.provi_Health$getHealth() == null) { // This can only occur if the mob is rendered before its first tick.
                mixinState.provi_Health$setShouldRenderHealth(false);
                mixinState.provi_Health$setShouldRenderLabel(false);
                mixinState.provi_Health$setIsLiving(false);
                return;
            }

            mixinState.provi_Health$getHealth().lerp(tickDelta);
            mixinState.provi_Health$setShouldRenderHealth(Options.shouldRenderHealthFor(living));
            mixinState.provi_Health$setIsLiving(true);
            mixinState.provi_Health$setShouldRenderLabel(this.hasLabel(entity, state.squaredDistanceToCamera));

            if (entity.getDisplayName() != null) mixinState.provi_Health$setLabel(entity.getDisplayName());
            else mixinState.provi_Health$setLabel(entity.getName());

            mixinState.provi_Health$setTitles(BorderRegistry.getTitle(living, true, false));

            // If another valid entity is riding this one, don't render a healthbar.
            if ((living.hasPassengers() &&
                living.getFirstPassenger() instanceof LivingEntity livingRider &&
                !Options.blacklist.contains(EntityType.getId(livingRider.getType()).toString())) ||
                living == MinecraftClient.getInstance().player ||
                !Visibility.isVisible(living)
            ) {
                mixinState.provi_Health$setShouldRenderHealth(false);
            }

            mixinState.provi_Health$setMountHealth(((IMixinLivingEntity)living).provi_Health$getMountHealthContainer());

            if (living.getScoreboardTeam() != null) mixinState.provi_Health$setTeamColour(living.getScoreboardTeam().getColor().getColorValue());
            else mixinState.provi_Health$setTeamColour(null);
        }
    }
}
