package com.provismet.provihealth.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.SeeThroughText;
import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.util.HealthContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;

public class EntityHealthBar {
    public static final float TEXTURE_SIZE = 64;
    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world.png");
    private static final int LIGHT = LightCoordsUtil.FULL_BRIGHT;
    private static final int BACKGROUND_BAR_INDEX = 1;
    private static final int FOREGROUND_BAR_INDEX = 0;

    public static void render (EntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, Quaternionf rotation, Font textRenderer) {
        IMixinEntityRenderState mixinState = (IMixinEntityRenderState)state;
        if (!mixinState.provi_Health$isLiving() || !mixinState.provi_Health$shouldRenderHealth() || Minecraft.getInstance().gui.hud.isHidden()) return;

        matrices.pushPose();
        matrices.translate(0f, state.boundingBoxHeight + 0.45f - (0.003f / Options.worldHealthBarScale) + Options.worldOffsetY, 0f);
        matrices.scale(Options.worldHealthBarScale, Options.worldHealthBarScale, Options.worldHealthBarScale);
        matrices.translate(0f, (mixinState.provi_Health$shouldRenderLabel() && !Options.overrideLabels && !(state.isInvisible || (state instanceof LivingEntityRenderState livingState && livingState.isInvisibleToPlayer)) ? 0.02f + 0.3f / Options.worldHealthBarScale : 0f), 0f);
        matrices.mulPose(rotation); // This is the problem.

        renderBars(mixinState, matrices, queue, false);
        if (mixinState.provi_Health$getMountHealth() != null && mixinState.provi_Health$getMountHealth().getMax() > 0) {
            matrices.pushPose();
            matrices.translate(0f, -1f * (7f / TEXTURE_SIZE), 0f);
            renderBars(mixinState, matrices, queue, true);
            matrices.popPose();
        }

        // Health Text
        if (Options.showTextInWorld) {
            matrices.pushPose();
            matrices.scale(0.01f, -0.01f, 0.01f);
            final String healthString = String.format("%d/%d", Math.round(mixinState.provi_Health$getHealth().getCurrent()), Math.round(mixinState.provi_Health$getHealth().getMax()));
            final float lineHeight = 9;

            List<Component> titles = List.of(); // initialise an empty list
            if (Options.worldTitles) titles = mixinState.provi_Health$getTitles();

            if (Options.overrideLabels) {
                final Component targetName = EntityHealthBar.getName(state);
                final float targetNameWidth = textRenderer.width(targetName);

                // 0 is in the centre.
                final float leftmost = -50f;
                final float rightmost = -leftmost;

                float healthX = rightmost - textRenderer.width(healthString);
                final float healthY = -lineHeight;
                float nameX = leftmost;
                float nameY = -lineHeight;
                boolean wrapLines = targetNameWidth - rightmost > healthX - 2f;

                if (wrapLines) {
                    healthX = (healthX - 50) / 2f;
                    nameX = -targetNameWidth / 2f;
                    nameY -= lineHeight;
                }

                if (mixinState.provi_Health$shouldRenderLabel() && !state.isDiscrete && Options.seeThroughTextType != SeeThroughText.NONE) {
                    if (Options.seeThroughTextType == SeeThroughText.STANDARD) {
                        if (Options.worldShadows) {
                            EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX + 1, nameY + 1, healthX + 1, healthY + 1, lineHeight, 1, 0xFF404040, false, matrices, queue, DisplayMode.NORMAL, LIGHT);
                        }

                        matrices.translate(0, 0, 0.03f);
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, CommonColors.WHITE, false, matrices, queue, DisplayMode.SEE_THROUGH, LIGHT);
                    }
                    else { // SeeThroughText.FULL
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, CommonColors.WHITE, Options.worldShadows, matrices, queue, DisplayMode.SEE_THROUGH, LIGHT);
                    }
                }
                else {
                    EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, CommonColors.WHITE, Options.worldShadows, matrices, queue, DisplayMode.NORMAL, LIGHT);
                }
            }
            else {
                queue.submitText(matrices, -(textRenderer.width(healthString)) / 2f, -lineHeight, Component.literal(healthString).getVisualOrderText(), Options.worldShadows, DisplayMode.NORMAL, LIGHT, CommonColors.WHITE, 0, 0);

                float titleX;
                float titleY = -lineHeight;
                for (Component title : titles) {
                    titleX = -textRenderer.width(title) / 2f;
                    titleY -= lineHeight;
                    queue.submitText(matrices, titleX, titleY, title.getVisualOrderText(), Options.worldShadows, DisplayMode.NORMAL, LIGHT, 0xFFFFFFFF, 0, 0);
                }
            }
            matrices.popPose();
        }

        matrices.popPose();
    }

    private static void renderFullText (Font textRenderer, Component name, String health, List<Component> titles, float nameX, float nameY, float healthX, float healthY, float titleLineHeight, float titleLineOffset, int colour, boolean shadow, PoseStack matrices, SubmitNodeCollector queue, DisplayMode layerType, int light) {
        queue.submitText(matrices, nameX, nameY, name.getVisualOrderText(), shadow, layerType, light, colour, 0, 0);
        queue.submitText(matrices, healthX, healthY, Component.literal(health).getVisualOrderText(), shadow, layerType, light, colour, 0, 0);

        float titleX;
        float titleY = nameY;
        for (Component title : titles) {
            titleX = titleLineOffset - (textRenderer.width(title) / 2f);
            titleY -= titleLineHeight;
            queue.submitText(matrices, titleX, titleY, title.getVisualOrderText(), shadow, layerType, light, colour, 0, 0);
        }
    }

    private static Component getName (EntityRenderState state) {
        if (state instanceof AvatarRenderState playerState && playerState.isInvisibleToPlayer) return Component.translatable("entity.provihealth.unknownPlayer");
        return ((IMixinEntityRenderState)state).provi_Health$getLabel();
    }

    private static void renderBars (IMixinEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, boolean isMount) {
        Optional<Integer> teamColour = Optional.ofNullable(state.provi_Health$getTeamColour());
        HealthContainer container = isMount ? state.provi_Health$getMountHealth() : state.provi_Health$getHealth();

        queue.submitCustomGeometry(
            matrices,
            RenderTypes.text(BARS),
            new RenderableHealthBar(
                teamColour,
                BACKGROUND_BAR_INDEX,
                1f,
                isMount
            )
        );
        queue.submitCustomGeometry(
            matrices,
            RenderTypes.text(BARS),
            new RenderableHealthBar(
                teamColour,
                FOREGROUND_BAR_INDEX,
                container.getLerped() / container.getMax(),
                isMount
            )
        );
    }
}
