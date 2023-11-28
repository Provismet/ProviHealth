package com.provismet.provihealth.world;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.provismet.provihealth.ProviHealthClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class EntityHealthBar {
    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/inworld.png");
    private static final float TEXTURE_SIZE = 64;
    private static final float MOUNT_BAR_PIXEL_LENGTH = 58;

    public static boolean enabled = true;

    @SuppressWarnings("resource")
    public static void render (Entity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Quaternionf rotation) {
        LivingEntity target;
        if (entity instanceof LivingEntity living) target = living;
        else return;
        if (!enabled || target.hasPassengers() || target == MinecraftClient.getInstance().player || target.isInvisibleTo(MinecraftClient.getInstance().player)) return;

        matrices.push();
        matrices.translate(0f, target.getHeight() + 0.5f + (target.shouldRenderName() || target.hasCustomName() && target == MinecraftClient.getInstance().targetedEntity ? 0.3f : 0f), 0f);
        matrices.scale(1.5f, 1.5f, 1f);
        matrices.multiply(rotation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();

        vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, BARS);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        Matrix4f model = matrices.peek().getPositionMatrix();
        renderBar(model, vertexConsumer, 1, 1f, false); // Empty
        renderBar(model, vertexConsumer, 0, MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0f, 1f), false); // Health

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
                matrices.translate(0f, -1f * (7f / TEXTURE_SIZE), 0f);
                Matrix4f mountModel = matrices.peek().getPositionMatrix();
                renderBar(mountModel, vertexConsumer, 1, 1f, true);
                renderBar(mountModel, vertexConsumer, 0, vehicleHealthPercent, true);
            }
        }

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    private static void renderBar (Matrix4f model, VertexConsumer vertexConsumer, int index, float percentage, boolean isMount) {
        // All U and V values are a percentage.
        final float MIN_U = isMount ? 3f / TEXTURE_SIZE : 0f; // Leftmost pixel
        final float MIN_V = ((index * 12f) / TEXTURE_SIZE) + (isMount ? 7f / TEXTURE_SIZE : 0f); // Topmost pixel
        final float MAX_U = percentage * (isMount ? 61f / TEXTURE_SIZE : 1f); // Leftmost pixel
        final float MAX_V = MIN_V + (isMount ? 5f : 7f) / TEXTURE_SIZE; // Bottommost pixel

        final float MIN_X = isMount ? 0.5f * (MOUNT_BAR_PIXEL_LENGTH / TEXTURE_SIZE) : 0.5f;
        final float MAX_X = MIN_X - percentage * (isMount ? MOUNT_BAR_PIXEL_LENGTH / TEXTURE_SIZE : 1f);
        final float MIN_Y = 0f;
        final float MAX_Y = -1f * ((isMount ? 5f : 7f) / TEXTURE_SIZE);

        final float Z = (float)index * 0.0001f;

        vertexConsumer.vertex(model, MAX_X, MAX_Y, Z).texture(MAX_U, MAX_V).next();
        vertexConsumer.vertex(model, MAX_X, MIN_Y, Z).texture(MAX_U, MIN_V).next();
        vertexConsumer.vertex(model, MIN_X, MIN_Y, Z).texture(MIN_U, MIN_V).next();
        vertexConsumer.vertex(model, MIN_X, MAX_Y, Z).texture(MIN_U, MAX_V).next();
    }
}
