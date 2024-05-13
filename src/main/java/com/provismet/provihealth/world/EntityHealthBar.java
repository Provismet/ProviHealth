package com.provismet.provihealth.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.SeeThroughText;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.util.Visibility;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class EntityHealthBar {
    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world.png");
    private static final Identifier COMPAT_BARS = ProviHealthClient.identifier("textures/gui/healthbars/in_world_coloured.png");
    private static final float TEXTURE_SIZE = 64;

    public static boolean enabled = true;

    @SuppressWarnings("resource")
    public static void render (Entity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Quaternion rotation) {
        LivingEntity target;
        if (entity instanceof LivingEntity living) target = living;
        else return;
        if (!enabled || (target.hasPassengers() && target.getFirstPassenger() instanceof LivingEntity livingRider && !Options.blacklist.contains(EntityType.getId(livingRider.getType()).toString())) || target == MinecraftClient.getInstance().player || !Visibility.isVisible(living)) return;
        if (!Options.shouldRenderHealthFor(living)) return;

        int light = LightmapTextureManager.pack(15, 15);

        matrices.push();
        matrices.translate(0f, target.getHeight() + 0.45f - (0.003f / Options.worldHealthBarScale) + Options.worldOffsetY, 0f);
        matrices.scale(Options.worldHealthBarScale, Options.worldHealthBarScale, Options.worldHealthBarScale);
        matrices.translate(0f, ((target.shouldRenderName() || target.hasCustomName() && target == MinecraftClient.getInstance().getEntityRenderDispatcher().targetedEntity) && !Options.overrideLabels && !target.isInvisibleTo(MinecraftClient.getInstance().player) ? 0.02f + 0.3f / Options.worldHealthBarScale : 0f), 0f);
        matrices.multiply(rotation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();

        if (Options.compatInWorld) {
            vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, COMPAT_BARS);
        }
        else {
            vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.setShaderTexture(0, BARS);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Matrix4f model = matrices.peek().getPositionMatrix();
        renderBar(model, vertexConsumer, 1, 1f, false); // Empty
        renderBar(model, vertexConsumer, 0, ((IMixinLivingEntity)target).provihealth_glideHealth(tickDelta * Options.worldGlide), false); // Health

        if (target.hasVehicle()) {
            float vehicleHealthDeep = 0f;
            float vehicleMaxHealthDeep = 0f;

            Entity currentEntity = target.getVehicle();
            while (currentEntity != null) {
                if (currentEntity instanceof LivingEntity currentLiving) {
                    vehicleHealthDeep += currentLiving.getHealth();
                    vehicleMaxHealthDeep += currentLiving.getMaxHealth();
                }
                currentEntity = currentEntity.getVehicle();
            }
            float vehicleHealthPercent = vehicleMaxHealthDeep > 0f ? MathHelper.clamp(vehicleHealthDeep / vehicleMaxHealthDeep, 0f, 1f) : 0f;

            if (vehicleHealthPercent > 0f) {
                matrices.push();
                matrices.translate(0f, -1f * (7f / TEXTURE_SIZE), 0f);
                Matrix4f mountModel = matrices.peek().getPositionMatrix();
                renderBar(mountModel, vertexConsumer, 1, 1f, true);
                renderBar(mountModel, vertexConsumer, 0, ((IMixinLivingEntity)target).provihealth_glideVehicle(vehicleHealthPercent, tickDelta * Options.worldGlide), true);
                matrices.pop();
            }
        }

        tessellator.draw();

        // Health Text
        if (Options.showTextInWorld) {
            matrices.push();
            matrices.scale(-0.01f, -0.01f, -0.01f);
            Matrix4f textModel = matrices.peek().getPositionMatrix();
            final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            final String healthString = String.format("%d/%d", Math.round(target.getHealth()), Math.round(target.getMaxHealth()));

            if (Options.overrideLabels) {
                final Text targetName = getName(target);
                final float targetNameWidth = textRenderer.getWidth(targetName);

                float healthX = 50f - textRenderer.getWidth(healthString);
                final float healthY = -9;
                float nameX = -50;
                float nameY = -9;
                boolean wrapLines = targetNameWidth - 50f > healthX - 2f;

                if (wrapLines) {
                    healthX = (healthX - 50) / 2f;
                    nameX = -targetNameWidth / 2f;
                    nameY -= 9;
                }

                if (target.shouldRenderName() && !target.isSneaky() && Options.seeThroughTextType != SeeThroughText.NONE) {
                    if (Options.seeThroughTextType == SeeThroughText.STANDARD) {
                        if (Options.worldShadows) {
                            textRenderer.draw(targetName, nameX + 1, nameY + 1, 0x404040, false, textModel, vertexConsumers, false, 0, light);
                            textRenderer.draw(healthString, healthX + 1, healthY + 1, 0x404040, false, textModel, vertexConsumers, false, 0, light);
                        }

                        matrices.translate(0, 0, 0.03f);
                        textModel = matrices.peek().getPositionMatrix();
                        textRenderer.draw(targetName, nameX, nameY, 0xFFFFFF, false, textModel, vertexConsumers, true, 0, light);
                        textRenderer.draw(healthString, healthX, healthY, 0xFFFFFF, false, textModel, vertexConsumers, true, 0, light);
                    }
                    else {
                        textRenderer.draw(targetName, nameX, nameY, 0xFFFFFF, Options.worldShadows, textModel, vertexConsumers, true, 0, light);
                        textRenderer.draw(healthString, healthX, healthY, 0xFFFFFF, Options.worldShadows, textModel, vertexConsumers, true, 0, light);
                    }
                }
                else {
                    textRenderer.draw(targetName, nameX, nameY, 0xFFFFFF, Options.worldShadows, textModel, vertexConsumers, false, 0, light);
                    textRenderer.draw(healthString, healthX, healthY, 0xFFFFFF, Options.worldShadows, textModel, vertexConsumers, false, 0, light);
                }
            }
            else textRenderer.draw(healthString, -(textRenderer.getWidth(healthString)) / 2f, -10, 0xFFFFFF, Options.worldShadows, textModel, vertexConsumers, false, 0, light);
            matrices.pop();
        }

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    @SuppressWarnings("resource")
    private static Text getName (LivingEntity entity) {
        if (entity instanceof PlayerEntity && entity.isInvisibleTo(MinecraftClient.getInstance().player)) return Text.translatable("entity.provihealth.unknownPlayer");
        else return entity.getDisplayName();
    }

    private static void renderBar (Matrix4f model, VertexConsumer vertexConsumer, int index, float percentage, boolean isMount) {
        if (isMount) percentage = MathHelper.lerp(percentage, 3f / TEXTURE_SIZE, 61f / TEXTURE_SIZE);

        // All U and V values are a percentage.
        final float MIN_U = 0f; // Leftmost pixel
        final float MIN_V = ((index * 12f) / TEXTURE_SIZE) + (isMount ? 7f / TEXTURE_SIZE : 0f); // Topmost pixel
        final float MAX_U = percentage; // Rightmost pixel
        final float MAX_V = MIN_V + (isMount ? 5f : 7f) / TEXTURE_SIZE; // Bottommost pixel

        // X and Y are block coordinates relative to the matrix shenanigans.
        final float MIN_X = 0.5f; // Pushes the bar half a block to the left, centering it.
        final float MAX_X = MIN_X - percentage;
        final float MIN_Y = 0f;
        final float MAX_Y = -1f * ((isMount ? 5f : 7f) / TEXTURE_SIZE); // Mount bar is 5 pixels tall, Health bar is 7 pixels tall.

        final float Z = (float)index * 0.0001f;

        if (Options.compatInWorld) {
            vertexConsumer.vertex(model, MAX_X, MAX_Y, Z).texture(MAX_U, MAX_V).next();
            vertexConsumer.vertex(model, MAX_X, MIN_Y, Z).texture(MAX_U, MIN_V).next();
            vertexConsumer.vertex(model, MIN_X, MIN_Y, Z).texture(MIN_U, MIN_V).next();
            vertexConsumer.vertex(model, MIN_X, MAX_Y, Z).texture(MIN_U, MAX_V).next();
        }
        else {
            Vec3f colour = Options.WHITE;
            if (index == 0) colour = Options.getBarColour(percentage, Options.unpackedStartWorld, Options.unpackedEndWorld, Options.worldGradient);

            vertexConsumer.vertex(model, MAX_X, MAX_Y, Z).color(colour.getX(), colour.getY(), colour.getZ(), 1f).texture(MAX_U, MAX_V).next();
            vertexConsumer.vertex(model, MAX_X, MIN_Y, Z).color(colour.getX(), colour.getY(), colour.getZ(), 1f).texture(MAX_U, MIN_V).next();
            vertexConsumer.vertex(model, MIN_X, MIN_Y, Z).color(colour.getX(), colour.getY(), colour.getZ(), 1f).texture(MIN_U, MIN_V).next();
            vertexConsumer.vertex(model, MIN_X, MAX_Y, Z).color(colour.getX(), colour.getY(), colour.getZ(), 1f).texture(MIN_U, MAX_V).next();
        }
    }
}
