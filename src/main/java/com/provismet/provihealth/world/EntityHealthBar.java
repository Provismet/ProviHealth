package com.provismet.provihealth.world;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.SeeThroughText;
import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.util.HealthContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;

public class EntityHealthBar {
    public static final float TEXTURE_SIZE = 64;
    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world.png");
    private static final int LIGHT = LightmapTextureManager.pack(15, 15);
    private static final int BACKGROUND_BAR_INDEX = 1;
    private static final int FOREGROUND_BAR_INDEX = 0;

    public static void render (EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, Quaternionf rotation, TextRenderer textRenderer) {
        IMixinEntityRenderState mixinState = (IMixinEntityRenderState)state;
        if (!mixinState.provi_Health$isLiving() || !mixinState.provi_Health$shouldRenderHealth() || !MinecraftClient.isHudEnabled()) return;

        matrices.push();
        matrices.translate(0f, state.height + 0.45f - (0.003f / Options.worldHealthBarScale) + Options.worldOffsetY, 0f);
        matrices.scale(Options.worldHealthBarScale, Options.worldHealthBarScale, Options.worldHealthBarScale);
        matrices.translate(0f, (mixinState.provi_Health$shouldRenderLabel() && !Options.overrideLabels && !(state.invisible || (state instanceof LivingEntityRenderState livingState && livingState.invisibleToPlayer)) ? 0.02f + 0.3f / Options.worldHealthBarScale : 0f), 0f);
        matrices.multiply(rotation); // This is the problem.

        renderBars(mixinState, matrices, queue, false);
        if (mixinState.provi_Health$getMountHealth() != null && mixinState.provi_Health$getMountHealth().getMax() > 0) {
            matrices.push();
            matrices.translate(0f, -1f * (7f / TEXTURE_SIZE), 0f);
            renderBars(mixinState, matrices, queue, true);
            matrices.pop();
        }

        // Health Text
        if (Options.showTextInWorld) {
            matrices.push();
            matrices.scale(0.01f, -0.01f, 0.01f);
            final String healthString = String.format("%d/%d", Math.round(mixinState.provi_Health$getHealth().getCurrent()), Math.round(mixinState.provi_Health$getHealth().getMax()));
            final float lineHeight = 9;

            List<Text> titles = List.of(); // initialise an empty list
            if (Options.worldTitles) titles = mixinState.provi_Health$getTitles();

            if (Options.overrideLabels) {
                final Text targetName = EntityHealthBar.getName(state);
                final float targetNameWidth = textRenderer.getWidth(targetName);

                // 0 is in the centre.
                final float leftmost = -50f;
                final float rightmost = -leftmost;

                float healthX = rightmost - textRenderer.getWidth(healthString);
                final float healthY = -lineHeight;
                float nameX = leftmost;
                float nameY = -lineHeight;
                boolean wrapLines = targetNameWidth - rightmost > healthX - 2f;

                if (wrapLines) {
                    healthX = (healthX - 50) / 2f;
                    nameX = -targetNameWidth / 2f;
                    nameY -= lineHeight;
                }

                if (mixinState.provi_Health$shouldRenderLabel() && !state.sneaking && Options.seeThroughTextType != SeeThroughText.NONE) {
                    if (Options.seeThroughTextType == SeeThroughText.STANDARD) {
                        if (Options.worldShadows) {
                            EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX + 1, nameY + 1, healthX + 1, healthY + 1, lineHeight, 1, 0xFF404040, false, matrices, queue, TextLayerType.NORMAL, LIGHT);
                        }

                        matrices.translate(0, 0, 0.03f);
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, Colors.WHITE, false, matrices, queue, TextLayerType.SEE_THROUGH, LIGHT);
                    }
                    else { // SeeThroughText.FULL
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, Colors.WHITE, Options.worldShadows, matrices, queue, TextLayerType.SEE_THROUGH, LIGHT);
                    }
                }
                else {
                    EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, Colors.WHITE, Options.worldShadows, matrices, queue, TextLayerType.NORMAL, LIGHT);
                }
            }
            else {
                queue.submitText(matrices, -(textRenderer.getWidth(healthString)) / 2f, -lineHeight, Text.literal(healthString).asOrderedText(), Options.worldShadows, TextLayerType.NORMAL, LIGHT, Colors.WHITE, 0, 0);

                float titleX;
                float titleY = -lineHeight;
                for (Text title : titles) {
                    titleX = -textRenderer.getWidth(title) / 2f;
                    titleY -= lineHeight;
                    queue.submitText(matrices, titleX, titleY, title.asOrderedText(), Options.worldShadows, TextLayerType.NORMAL, LIGHT, 0xFFFFFFFF, 0, 0);
                }
            }
            matrices.pop();
        }

        matrices.pop();
    }

    private static void renderFullText (TextRenderer textRenderer, Text name, String health, List<Text> titles, float nameX, float nameY, float healthX, float healthY, float titleLineHeight, float titleLineOffset, int colour, boolean shadow, MatrixStack matrices, OrderedRenderCommandQueue queue, TextLayerType layerType, int light) {
        queue.submitText(matrices, nameX, nameY, name.asOrderedText(), shadow, layerType, light, colour, 0, 0);
        queue.submitText(matrices, healthX, healthY, Text.literal(health).asOrderedText(), shadow, layerType, light, colour, 0, 0);

        float titleX;
        float titleY = nameY;
        for (Text title : titles) {
            titleX = titleLineOffset - (textRenderer.getWidth(title) / 2f);
            titleY -= titleLineHeight;
            queue.submitText(matrices, titleX, titleY, title.asOrderedText(), shadow, layerType, light, colour, 0, 0);
        }
    }

    private static Text getName (EntityRenderState state) {
        if (state instanceof PlayerEntityRenderState playerState && playerState.invisibleToPlayer) return Text.translatable("entity.provihealth.unknownPlayer");
        return ((IMixinEntityRenderState)state).provi_Health$getLabel();
    }

    private static void renderBars (IMixinEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean isMount) {
        Optional<Integer> teamColour = Optional.ofNullable(state.provi_Health$getTeamColour());
        HealthContainer container = isMount ? state.provi_Health$getMountHealth() : state.provi_Health$getHealth();

        queue.submitCustom(
            matrices,
            RenderLayers.text(BARS),
            new RenderableHealthBar(
                teamColour,
                BACKGROUND_BAR_INDEX,
                1f,
                isMount
            )
        );
        queue.submitCustom(
            matrices,
            RenderLayers.text(BARS),
            new RenderableHealthBar(
                teamColour,
                FOREGROUND_BAR_INDEX,
                container.getLerped() / container.getMax(),
                isMount
            )
        );
    }
}
