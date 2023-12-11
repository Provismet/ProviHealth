package com.provismet.provihealth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.hud.TargetHealthBar;

import net.minecraft.client.render.entity.EntityRenderer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method="renderLabelIfPresent", at=@At("HEAD"), cancellable=true)
    private void cancelLabel (CallbackInfo info) {
        if (TargetHealthBar.disabledLabels || Options.overrideLabels) info.cancel();
    }
}
