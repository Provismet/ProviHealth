package com.provismet.provihealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.ElementRegistry;
import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.util.Visibility;
import com.provismet.provihealth.world.EntityHealthBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow @Final
    private Font font;

    @Shadow
    protected abstract boolean shouldShowName (Entity entity, double squaredDistanceToCamera);

    @Inject(method="submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at=@At("HEAD"), cancellable=true)
    private void cancelLabel (EntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo info) {
        if (Options.overrideLabels &&  ((IMixinEntityRenderState)state).provi_Health$shouldRenderHealth()) info.cancel();
    }

    @Inject(method="submit", at=@At("HEAD"))
    private void addHealthBar (EntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState, CallbackInfo info) {
        EntityHealthBar.render(state, matrices, queue, cameraState.orientation, this.font);
    }

    @Inject(method="extractRenderState", at=@At("HEAD"))
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
            mixinState.provi_Health$setShouldRenderLabel(this.shouldShowName(entity, state.distanceToCameraSq));

            if (entity.getDisplayName() != null) mixinState.provi_Health$setLabel(entity.getDisplayName());
            else mixinState.provi_Health$setLabel(entity.getName());

            mixinState.provi_Health$setTitles(ElementRegistry.getTitle(living, true, false));

            // If another valid entity is riding this one, don't render a healthbar.
            if ((living.isVehicle() &&
                living.getFirstPassenger() instanceof LivingEntity livingRider &&
                !Options.isBlacklisted(livingRider, Options.BarType.WORLD)) ||
                living == Minecraft.getInstance().player ||
                !Visibility.isVisible(living)
            ) {
                mixinState.provi_Health$setShouldRenderHealth(false);
            }

            mixinState.provi_Health$setMountHealth(((IMixinLivingEntity)living).provi_Health$getMountHealthContainer());
            if (mixinState.provi_Health$getMountHealth() != null) {
                mixinState.provi_Health$getMountHealth().lerp(tickDelta);
            }

            if (living.getTeam() != null) mixinState.provi_Health$setTeamColour(living.getTeam().getColor().getColor());
            else mixinState.provi_Health$setTeamColour(null);
        }
    }
}
