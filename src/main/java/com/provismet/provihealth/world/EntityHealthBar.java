package com.provismet.provihealth.world;

import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.SeeThroughText;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class EntityHealthBar {
    public static boolean enabled = true;

    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world.png");
    private static final Identifier COMPAT_BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world_coloured.png");
    private static final float TEXTURE_SIZE = 64;
    private static final int LIGHT = LightmapTextureManager.pack(15, 15);

    public static void render (EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Quaternionf rotation, TextRenderer textRenderer) {
        IMixinEntityRenderState mixinState = (IMixinEntityRenderState)state;
        if (!enabled || !mixinState.provi_Health$isLiving() || !mixinState.provi_Health$shouldRenderHealth() || !MinecraftClient.isHudEnabled()) return;

        matrices.push();
        matrices.translate(0f, state.height + 0.45f - (0.003f / Options.worldHealthBarScale) + Options.worldOffsetY, 0f);
        matrices.scale(Options.worldHealthBarScale, Options.worldHealthBarScale, Options.worldHealthBarScale);
        matrices.translate(0f, (mixinState.provi_Health$shouldRenderLabel() && !Options.overrideLabels && !(state.invisible || (state instanceof LivingEntityRenderState livingState && livingState.invisibleToPlayer)) ? 0.02f + 0.3f / Options.worldHealthBarScale : 0f), 0f);
        matrices.multiply(rotation); // This is the problem.

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer;

        if (Options.compatInWorld) {
            vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
            RenderSystem.setShaderTexture(0, COMPAT_BARS);
        }
        else {
            vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, BARS);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Matrix4f model = matrices.peek().getPositionMatrix();
        renderBar(model, vertexConsumer, 1, 1f, false); // Empty
        renderBar(model, vertexConsumer, 0, mixinState.provi_Health$getHealth().getLerped() / mixinState.provi_Health$getHealth().getMax(), false); // Health

        if (mixinState.provi_Health$getMountHealth() != null && mixinState.provi_Health$getMountHealth().getMax() > 0) {
            matrices.push();
            matrices.translate(0f, -1f * (7f / TEXTURE_SIZE), 0f);
            Matrix4f mountModel = matrices.peek().getPositionMatrix();
            renderBar(mountModel, vertexConsumer, 1, 1f, true); // Empty
            renderBar(mountModel, vertexConsumer, 0, mixinState.provi_Health$getMountHealth().getLerped() / mixinState.provi_Health$getMountHealth().getMax(), true); // Health
            matrices.pop();
        }

        BufferRenderer.drawWithGlobalProgram(vertexConsumer.end());

        // Health Text
        if (Options.showTextInWorld) {
            matrices.push();
            matrices.scale(0.01f, -0.01f, 0.01f);
            Matrix4f textModel = matrices.peek().getPositionMatrix();
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
                            EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX + 1, nameY + 1, healthX + 1, healthY + 1, lineHeight, 1, 0xFF404040, false, textModel, vertexConsumers, TextLayerType.NORMAL, LIGHT);
                        }

                        matrices.translate(0, 0, 0.03f);
                        textModel = matrices.peek().getPositionMatrix();
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, 0xFFFFFFFF, false, textModel, vertexConsumers, TextLayerType.SEE_THROUGH, LIGHT);
                    }
                    else { // SeeThroughText.FULL
                        EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, 0xFFFFFFFF, Options.worldShadows, textModel, vertexConsumers, TextLayerType.SEE_THROUGH, LIGHT);
                    }
                }
                else {
                    EntityHealthBar.renderFullText(textRenderer, targetName, healthString, titles, nameX, nameY, healthX, healthY, lineHeight, 0, 0xFFFFFFFF, Options.worldShadows, textModel, vertexConsumers, TextLayerType.NORMAL, LIGHT);
                }
            }
            else {
                textRenderer.draw(healthString, -(textRenderer.getWidth(healthString)) / 2f, -lineHeight, 0xFFFFFFFF, Options.worldShadows, textModel, vertexConsumers, TextLayerType.NORMAL, 0, LIGHT);

                float titleX;
                float titleY = -lineHeight;
                for (Text title : titles) {
                    titleX = -textRenderer.getWidth(title) / 2f;
                    titleY -= lineHeight;
                    textRenderer.draw(title, titleX, titleY, 0xFFFFFFFF, Options.worldShadows, textModel, vertexConsumers, TextLayerType.NORMAL, 0, LIGHT);
                }
            }
            matrices.pop();
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    private static void renderFullText (TextRenderer textRenderer, Text name, String health, List<Text> titles, float nameX, float nameY, float healthX, float healthY, float titleLineHeight, float titleLineOffset, int colour, boolean shadow, Matrix4f model, VertexConsumerProvider vertexes, TextLayerType layerType, int light) {
        textRenderer.draw(name, nameX, nameY, colour, shadow, model, vertexes, layerType, 0, light);
        textRenderer.draw(health, healthX, healthY, colour, shadow, model, vertexes, layerType, 0, light);

        float titleX;
        float titleY = nameY;
        for (Text title : titles) {
            titleX = titleLineOffset - (textRenderer.getWidth(title) / 2f);
            titleY -= titleLineHeight;
            textRenderer.draw(title, titleX, titleY, colour, shadow, model, vertexes, layerType, 0, light);
        }
    }

    private static Text getName (EntityRenderState state) {
        if (state instanceof PlayerEntityRenderState playerState && playerState.invisibleToPlayer) return Text.translatable("entity.provihealth.unknownPlayer");
        return ((IMixinEntityRenderState)state).provi_Health$getLabel();
    }

    private static void renderBar (Matrix4f model, VertexConsumer vertexConsumer, int index, float percentage, boolean isMount) {
        percentage = MathHelper.clamp(percentage, 0f, 1f);
        if (isMount) percentage = MathHelper.lerp(percentage, 3f / TEXTURE_SIZE, 61f / TEXTURE_SIZE);

        // As of 1.21, the rendering was changed for whatever reason and the bars were facing in the wrong direction (which makes them invisible).
        // This method now renders them backwards because simply rotating them was causing even more issues.

        // All U and V values are a percentage.
        final float MIN_U = 1f - percentage; // Leftmost pixel
        final float MIN_V = ((index * 12f) / TEXTURE_SIZE) + (isMount ? 7f / TEXTURE_SIZE : 0f); // Topmost pixel
        final float MAX_U = 1f; // Rightmost pixel
        final float MAX_V = MIN_V + (isMount ? 5f : 7f) / TEXTURE_SIZE; // Bottommost pixel

        // X and Y are block coordinates relative to the matrix shenanigans.
        final float MAX_X = -0.5f; // Pushes the bar half a block to the left, centering it.
        final float MIN_X = MAX_X + percentage;
        final float MIN_Y = 0f;
        final float MAX_Y = -1f * ((isMount ? 5f : 7f) / TEXTURE_SIZE); // Mount bar is 5 pixels tall, Health bar is 7 pixels tall.

        final float Z = (float)index * -0.0001f;

        if (Options.compatInWorld) {
            vertexConsumer.vertex(model, MAX_X, MAX_Y, Z).texture(MAX_U, MAX_V); // Bottom-Right
            vertexConsumer.vertex(model, MIN_X, MAX_Y, Z).texture(MIN_U, MAX_V); // Bottom-Left
            vertexConsumer.vertex(model, MIN_X, MIN_Y, Z).texture(MIN_U, MIN_V); // Top-Left
            vertexConsumer.vertex(model, MAX_X, MIN_Y, Z).texture(MAX_U, MIN_V); // Top-Right
        }
        else {
            Vector3f colour = Options.WHITE;
            if (index == 0) colour = Options.getBarColour(percentage, Options.unpackedStartWorld, Options.unpackedEndWorld, Options.worldGradient);

            vertexConsumer.vertex(model, MIN_X, MIN_Y, Z).texture(MIN_U, MIN_V).color(colour.x, colour.y, colour.z, 1f); // Top-Left
            vertexConsumer.vertex(model, MAX_X, MIN_Y, Z).texture(MAX_U, MIN_V).color(colour.x, colour.y, colour.z, 1f); // Top-Right
            vertexConsumer.vertex(model, MAX_X, MAX_Y, Z).texture(MAX_U, MAX_V).color(colour.x, colour.y, colour.z, 1f); // Bottom-Right
            vertexConsumer.vertex(model, MIN_X, MAX_Y, Z).texture(MIN_U, MAX_V).color(colour.x, colour.y, colour.z, 1f); // Bottom-Left
        }
    }
}
