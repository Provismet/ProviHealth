package com.provismet.provihealth.hud;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.world.EntityHealthBar;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TargetHealthBar implements HudRenderCallback {
    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/bars.png");
    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 10;
    private static final int MOUNT_BAR_HEIGHT = 6;
    private static final int MOUNT_BAR_WIDTH = 121;
    private static final int FRAME_LENGTH = 48;
    private static final int BAR_X = FRAME_LENGTH - 10;
    private static final int BAR_Y = FRAME_LENGTH / 2 - (BAR_HEIGHT + MOUNT_BAR_HEIGHT) / 2;

    private LivingEntity target = null;
    private float healthBarDuration = 0f;

    private int currentHealthWidth;
    private int currentVehicleHealthWidth;

    @SuppressWarnings("resource")
    @Override
    public void onHudRender (DrawContext drawContext, float tickDelta) {
        if (!Options.shouldRenderHUD || !MinecraftClient.isHudEnabled() || MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud() || MinecraftClient.getInstance().player.isSpectator()) return;

        boolean isNew = false;

        if (MinecraftClient.getInstance().targetedEntity instanceof LivingEntity living) {
            if (living.isInvisibleTo(MinecraftClient.getInstance().player)) return;
            if (!living.equals(this.target)) isNew = true;
            this.target = living;
            this.healthBarDuration = Options.maxHealthBarTicks;
        }

        if (this.healthBarDuration > 0f) {
            if (this.target == null) {
                this.reset();
                return;
            }

            float healthPercent = MathHelper.clamp(this.target.getHealth() / this.target.getMaxHealth(), 0f, 1f);

            float vehicleHealthDeep = 0f;
            float vehicleMaxHealthDeep = 0f;

            Entity currentEntity = this.target.getVehicle();
            while (currentEntity != null) {
                if (currentEntity instanceof LivingEntity currentLiving) {
                    vehicleHealthDeep += currentLiving.getHealth();
                    vehicleMaxHealthDeep += currentLiving.getMaxHealth();
                }
                currentEntity = currentEntity.getVehicle();
            }
            float vehicleHealthPercent = vehicleMaxHealthDeep > 0f ? MathHelper.clamp(vehicleHealthDeep / vehicleMaxHealthDeep, 0f, 1f) : 0f;

            int healthWidth = Math.round(BAR_WIDTH * healthPercent);
            int vehicleHealthWidth = Math.round(MOUNT_BAR_WIDTH * vehicleHealthPercent);

            if (isNew) {
                this.currentHealthWidth = healthWidth;
                this.currentVehicleHealthWidth = vehicleHealthWidth;
            }

            this.renderBar(drawContext, BAR_WIDTH, 1); // Empty space
            this.renderBar(drawContext, glideHealth(healthWidth, tickDelta * 0.5f), 0); // Health
            if (vehicleMaxHealthDeep > 0f) {
                this.renderMountBar(drawContext, MOUNT_BAR_WIDTH, 1); // Empty space
                this.renderMountBar(drawContext, glideVehicleHealth(vehicleHealthWidth, tickDelta * 0.5f), 0); // Health
            }

            drawContext.drawTexture(BorderRegistry.getBorder(target.getGroup()), 0, 0, FRAME_LENGTH, FRAME_LENGTH, 48f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Portrait Background
            drawContext.drawText(MinecraftClient.getInstance().textRenderer, target.getName(), FRAME_LENGTH, BAR_Y - BAR_HEIGHT, 0xFFFFFF, true); // Name
            drawContext.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d/%d", Math.round(this.target.getHealth()), Math.round(this.target.getMaxHealth())), FRAME_LENGTH, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 2, 0xFFFFFF, true); // Health Value
            if (BorderRegistry.getItem(this.target.getGroup()) != null) drawContext.drawItem(BorderRegistry.getItem(this.target.getGroup()), BAR_X + BAR_WIDTH - 16, BAR_Y - 16);

            float prevTargetHeadYaw = this.target.getHeadYaw();
            float prevPrevTargetHeadYaw = this.target.prevHeadYaw;
            float prevTargetBodyYaw = this.target.getBodyYaw();
            float prevPrevTargetBodyYaw = this.target.prevBodyYaw;
            float prevTargetYaw = this.target.getYaw();

            float headBodyYawDifference = this.target.getHeadYaw() - this.target.getBodyYaw();

            this.target.setYaw(135f);
            this.target.setBodyYaw(this.target.getYaw());
            this.target.prevBodyYaw = this.target.getYaw();
            this.target.setHeadYaw(this.target.getYaw() + headBodyYawDifference);
            this.target.prevHeadYaw = this.target.getYaw() + headBodyYawDifference;

            drawContext.enableScissor(0, 0, FRAME_LENGTH, FRAME_LENGTH);
            EntityHealthBar.enabled = false;
            InventoryScreen.drawEntity(drawContext, 24, 32, 30,
                new Vector3f(0.0F, this.target.getHeight() / 2f + 0.0625f, 0f),
                (new Quaternionf()).rotateZ(3.1415927f),
                null,
                this.target
            );
            EntityHealthBar.enabled = true;
            drawContext.disableScissor();

            this.target.setHeadYaw(prevTargetHeadYaw);
            this.target.prevHeadYaw = prevPrevTargetHeadYaw;
            this.target.setBodyYaw(prevTargetBodyYaw);
            this.target.prevBodyYaw = prevPrevTargetBodyYaw;
            this.target.setYaw(prevTargetYaw);

            drawContext.drawTexture(BorderRegistry.getBorder(this.target.getGroup()), 0, 0, 300, 0f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Portrait foreground
        }

        if (this.healthBarDuration > 0f) this.healthBarDuration -= tickDelta;
        else this.reset();
    }

    private int glideHealth (int trueValue, float glideFactor) {
        this.currentHealthWidth += (float)(trueValue - this.currentHealthWidth) * MathHelper.clamp(glideFactor, 0.001f, 1f);
        return this.currentHealthWidth;
    }

    private int glideVehicleHealth (int trueValue, float glideFactor) {
        this.currentVehicleHealthWidth += (float)(trueValue - this.currentVehicleHealthWidth) * MathHelper.clamp(glideFactor, 0.001f, 1f);
        return this.currentVehicleHealthWidth;
    }

    private void renderBar (DrawContext drawContext, int width, int barIndex) {
        drawContext.drawTexture(BARS, BAR_X, BAR_Y, width, BAR_HEIGHT, 0f, barIndex * (BAR_HEIGHT + MOUNT_BAR_HEIGHT), width, BAR_HEIGHT, BAR_WIDTH, 32);
    }

    private void renderMountBar (DrawContext drawContext, int width, int barIndex) {
        drawContext.drawTexture(BARS, BAR_X, BAR_Y + BAR_HEIGHT, width, MOUNT_BAR_HEIGHT, 0f, barIndex * (BAR_HEIGHT + MOUNT_BAR_HEIGHT) + BAR_HEIGHT, width, MOUNT_BAR_HEIGHT, BAR_WIDTH, 32);
    }

    private void reset () {
        this.healthBarDuration = 0f;
        this.target = null;
        this.currentHealthWidth = 0;
        this.currentVehicleHealthWidth = 0;
    }
}
