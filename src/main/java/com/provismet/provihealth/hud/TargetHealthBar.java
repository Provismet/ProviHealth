package com.provismet.provihealth.hud;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.HUDPortraitCompatMode;
import com.provismet.provihealth.config.Options.HUDPosition;
import com.provismet.provihealth.config.Options.HUDType;
import com.provismet.provihealth.util.Visibility;
import com.provismet.provihealth.world.EntityHealthBar;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TargetHealthBar implements HudRenderCallback {
    public static boolean disabledLabels = false;

    private static final Identifier BARS = ProviHealthClient.identifier("textures/gui/healthbars/bars.png");
    private static final Identifier COMPAT_BARS = ProviHealthClient.identifier("textures/gui/healthbars/bars_coloured.png");
    private static final Identifier HEART = ProviHealthClient.identifier("textures/gui/healthbars/icons/heart.png");
    private static final Identifier MOUNT_HEART = ProviHealthClient.identifier("textures/gui/healthbars/icons/mount_heart.png");
    private static final Identifier ARMOUR = ProviHealthClient.identifier("textures/gui/healthbars/icons/armour.png");

    private static int OFFSET_X = 0;
    private static int OFFSET_Y = 0;
    private static final int BAR_WIDTH = 128;
    private static final int BAR_HEIGHT = 10;
    private static final int MOUNT_BAR_HEIGHT = 6;
    private static final int MOUNT_BAR_WIDTH = 121;
    private static final int FRAME_LENGTH = 48;
    private static final int LEFT_TEXT_X = FRAME_LENGTH + 2;
    private static int BAR_X = FRAME_LENGTH - 5;
    private static int BAR_Y = OFFSET_Y + FRAME_LENGTH / 2 - (BAR_HEIGHT + MOUNT_BAR_HEIGHT) / 2;

    private static final float BAR_V2 = ((float)BAR_HEIGHT / (float)(BAR_HEIGHT + MOUNT_BAR_HEIGHT)) / 2f; // Accounting for index.
    private static final float MOUNT_BAR_U2 = (float)MOUNT_BAR_WIDTH / (float)BAR_WIDTH;
    private static final float MOUNT_BAR_V1 = ((float)BAR_HEIGHT / (float)(BAR_HEIGHT + MOUNT_BAR_HEIGHT)) / 2f;
    private static final float MOUNT_BAR_V2 = 0.5f; // Accounting for index.

    private static final int BAR_WIDTH_DIFF = BAR_WIDTH - MOUNT_BAR_WIDTH;

    private LivingEntity target = null;
    private float healthBarDuration = 0f;

    private int currentHealthWidth;
    private int currentVehicleHealthWidth;

    @SuppressWarnings("resource")
    @Override
    public void onHudRender (DrawContext drawContext, float tickDelta) {
        if (this.healthBarDuration > 0f) this.healthBarDuration -= tickDelta;
        else this.reset();

        if (!MinecraftClient.isHudEnabled() || MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud() || MinecraftClient.getInstance().player.isSpectator()) return;

        boolean isNew = false;

        if (MinecraftClient.getInstance().targetedEntity instanceof LivingEntity living) {
            if (!Visibility.isVisible(living)) return;
            if (!living.equals(this.target)) isNew = true;
            this.target = living;
            this.healthBarDuration = Options.maxHealthBarTicks;
        }

        if (this.healthBarDuration > 0f) {
            if (this.target == null) {
                this.reset();
                return;
            }

            this.adjustForScreenSize();
            HUDType hudType = Options.getHUDFor(this.target);

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

            final int nameWidth = MinecraftClient.getInstance().textRenderer.getWidth(this.getName(this.target));
            if (hudType == HUDType.FULL) {
                // Render bars
                this.renderBar(drawContext, BAR_WIDTH, 1); // Empty space
                this.renderBar(drawContext, glideHealth(healthWidth, tickDelta * Options.hudGlide), 0); // Health
                if (vehicleMaxHealthDeep > 0f) {
                    this.renderMountBar(drawContext, MOUNT_BAR_WIDTH, 1); // Empty space
                    this.renderMountBar(drawContext, glideVehicleHealth(vehicleHealthWidth, tickDelta * Options.hudGlide), 0); // Health
                }

                int infoLeftX = LEFT_TEXT_X;
                if (Options.hudPosition == HUDPosition.LEFT) {
                    // Render entity group icon
                    int expectedNameX = LEFT_TEXT_X + nameWidth + 2; // Starting point + width + 2 pixels of free space.
                    if (BorderRegistry.getItem(this.target) != null && Options.showHudIcon) drawContext.drawItem(BorderRegistry.getItem(this.target), Math.max(BAR_X + BAR_WIDTH - 16, expectedNameX), BAR_Y - 16);
                }
                else {
                    int expectedNameX = OFFSET_X - 18 - nameWidth; // Leftmost pixel of name, then left by 2 pixels, then left by 16 to make space for the icon.
                    if (BorderRegistry.getItem(this.target) != null && Options.showHudIcon) drawContext.drawItem(BorderRegistry.getItem(this.target), Math.min(BAR_X, expectedNameX), BAR_Y - 16);
                    infoLeftX = BAR_X + 3;
                }

                // Render health value and heart icons
                int healthX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d/%d", Math.round(this.target.getHealth()), Math.round(this.target.getMaxHealth())), infoLeftX, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 2, 0xFFFFFF, true); // Health Value
                drawContext.drawTexture(HEART, healthX, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 1, 9, 9, 0f, 0f, 9, 9, 9, 9);

                // Render armour icon if necessary
                int armourX = MinecraftClient.getInstance().textRenderer.getWidth(String.format("%d/%d", Math.round(this.target.getMaxHealth()), Math.round(this.target.getMaxHealth()))) + infoLeftX + 18;
                if (this.target.getArmor() > 0) {
                    armourX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d", this.target.getArmor()), armourX, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 2, 0xFFFFFF, true);
                    drawContext.drawTexture(ARMOUR, armourX, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 1, 9, 9, 0f, 0f, 9, 9, 9, 9);
                }

                if (vehicleMaxHealthDeep > 0f) {
                    String mountHealthString = String.format("%d/%d", Math.round(vehicleHealthDeep), Math.round(vehicleMaxHealthDeep));
                    int mountHealthWidth = MinecraftClient.getInstance().textRenderer.getWidth(mountHealthString) + 9;
                    int expectedLeftPixel = BAR_X + BAR_WIDTH - mountHealthWidth - 3;

                    if (expectedLeftPixel < armourX) expectedLeftPixel = armourX + 10;

                    int mountHealthX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, mountHealthString, expectedLeftPixel, BAR_Y + BAR_HEIGHT + (vehicleMaxHealthDeep > 0f ? MOUNT_BAR_HEIGHT : 0) + 2, 0xFFFFFF, true);
                    drawContext.drawTexture(MOUNT_HEART, mountHealthX, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT + 1, 9, 9, 0f, 0f, 9, 9, 9, 9);
                }
            }
            
            if (hudType != HUDType.NONE) {
                // Render Portrait
                RenderSystem.enableBlend();
                if (Options.hudPosition == HUDPosition.LEFT) {
                    drawContext.drawTexture(BorderRegistry.getBorder(this.target), 0, OFFSET_Y, FRAME_LENGTH, FRAME_LENGTH, 48f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Background
                    drawContext.drawTexture(BorderRegistry.getBorder(this.target), 0, OFFSET_Y, 300, 0f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Foreground
                    RenderSystem.disableBlend();

                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, this.getName(this.target), LEFT_TEXT_X, BAR_Y - BAR_HEIGHT, 0xFFFFFF, true); // Name
                }
                else {
                    this.drawHorizontallyMirroredTexturedQuad(BorderRegistry.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, 0, 0.5f, 1f, 0f, 1f); // Background
                    this.drawHorizontallyMirroredTexturedQuad(BorderRegistry.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, 300, 0f, 0.5f, 0f, 1f); // Foreground
                    RenderSystem.disableBlend();

                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, this.getName(this.target), OFFSET_X - 1 - nameWidth, BAR_Y - BAR_HEIGHT, 0xFFFFFF, true); // Name
                }

                // Render Paper Doll
                if (Options.HUDCompat == HUDPortraitCompatMode.STANDARD) {
                    float prevTargetHeadYaw = this.target.headYaw;
                    float prevPrevTargetHeadYaw = this.target.prevHeadYaw;
                    float prevTargetBodyYaw = this.target.bodyYaw;
                    float prevPrevTargetBodyYaw = this.target.prevBodyYaw;

                    this.target.bodyYaw = Options.hudPosition.portraitYAW;
                    this.target.prevBodyYaw = Options.hudPosition.portraitYAW;
                    this.target.headYaw = Options.hudPosition.portraitYAW;
                    this.target.prevHeadYaw = Options.hudPosition.portraitYAW;

                    float renderHeight;
                    if (this.target.getEyeHeight(EntityPose.STANDING) >= this.target.getHeight() * 0.6) {
                        renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.5f;
                        if (renderHeight < 1f) renderHeight = 1f;
                    }
                    else renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.8f;

                    drawContext.enableScissor(OFFSET_X, OFFSET_Y, OFFSET_X + FRAME_LENGTH, OFFSET_Y + FRAME_LENGTH);
                    EntityHealthBar.enabled = false;
                    disabledLabels = true;
                    this.drawEntity(drawContext, 24 + OFFSET_X, OFFSET_Y, 30,
                        new Vector3f(0f, renderHeight, 0f),
                        (new Quaternionf()).rotateZ(3.1415927f),
                        null,
                        this.target
                    );
                    EntityHealthBar.enabled = true;
                    disabledLabels = false;
                    drawContext.disableScissor();

                    this.target.headYaw = prevTargetHeadYaw;
                    this.target.prevHeadYaw = prevPrevTargetHeadYaw;
                    this.target.bodyYaw = prevTargetBodyYaw;
                    this.target.prevBodyYaw = prevPrevTargetBodyYaw;
                }
                else if (Options.HUDCompat == HUDPortraitCompatMode.COMPAT) {
                    float yawOffset = -(Options.hudPosition.portraitYAW - this.target.getBodyYaw()) / MathHelper.DEGREES_PER_RADIAN;

                    float renderHeight;
                    if (this.target.getEyeHeight(EntityPose.STANDING) >= this.target.getHeight() * 0.6) {
                        renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.5f;
                        if (renderHeight < 1f) renderHeight = 1f;
                    }
                    else renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.8f;

                    drawContext.enableScissor(OFFSET_X, OFFSET_Y, OFFSET_X + FRAME_LENGTH, OFFSET_Y + FRAME_LENGTH);
                    EntityHealthBar.enabled = false;
                    disabledLabels = true;
                    this.drawEntity(drawContext, 24 + OFFSET_X, OFFSET_Y, 30,
                        new Vector3f(0f, renderHeight, 0f),
                        (new Quaternionf()).rotateZ(3.1415927f).rotateY(yawOffset),
                        null,
                        this.target
                    );
                    EntityHealthBar.enabled = true;
                    disabledLabels = false;
                    drawContext.disableScissor();
                }
            }
        }
    }

    @SuppressWarnings("resource")
    private Text getName (LivingEntity entity) {
        if (entity instanceof PlayerEntity && entity.isInvisibleTo(MinecraftClient.getInstance().player)) return Text.translatable("entity.provihealth.unknownPlayer");
        else return entity.getDisplayName();
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
        if (Options.hudPosition == HUDPosition.LEFT) this.drawTexturedQuad(BARS, drawContext, BAR_X, BAR_X + width, BAR_Y, BAR_Y + BAR_HEIGHT, 0, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, Options.getBarColour((float)width / (float)BAR_WIDTH, barIndex == 1 ? Options.WHITE : Options.unpackedStartHud, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient));
        else this.drawHorizontallyMirroredTexturedQuad(BARS, drawContext, BAR_X + (BAR_WIDTH - width), BAR_X + BAR_WIDTH, BAR_Y, BAR_Y + BAR_HEIGHT, 0, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, Options.getBarColour((float)width / (float)BAR_WIDTH, barIndex == 1 ? Options.WHITE : Options.unpackedStartHud, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient));
    }

    private void renderMountBar (DrawContext drawContext, int width, int barIndex) {
        if (Options.hudPosition == HUDPosition.LEFT) this.drawTexturedQuad(BARS, drawContext, BAR_X, BAR_X + width, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, Options.getBarColour((float)width / (float)MOUNT_BAR_WIDTH, barIndex == 1 ? Options.WHITE : Options.unpackedStartHud, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient));
        else this.drawHorizontallyMirroredTexturedQuad(BARS, drawContext, BAR_X + (MOUNT_BAR_WIDTH - width) + BAR_WIDTH_DIFF, BAR_X + BAR_WIDTH_DIFF + MOUNT_BAR_WIDTH, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, Options.getBarColour((float)width / (float)MOUNT_BAR_WIDTH, barIndex == 1 ? Options.WHITE : Options.unpackedStartHud, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient));
    }

    private void reset () {
        this.healthBarDuration = 0f;
        this.target = null;
        this.currentHealthWidth = 0;
        this.currentVehicleHealthWidth = 0;
    }

    private void adjustForScreenSize () {
        OFFSET_Y = Math.min((int)(MinecraftClient.getInstance().getWindow().getScaledHeight() * (Options.hudOffsetPercent / 100f)), MinecraftClient.getInstance().getWindow().getScaledHeight() - FRAME_LENGTH);
        BAR_Y = OFFSET_Y + FRAME_LENGTH / 2 - (BAR_HEIGHT + MOUNT_BAR_HEIGHT) / 2;

        if (Options.hudPosition == HUDPosition.LEFT) {
            OFFSET_X = 0;
            BAR_X = FRAME_LENGTH - 5;
        }
        else {
            int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
            OFFSET_X = width - FRAME_LENGTH;
            BAR_X = OFFSET_X + 5 - BAR_WIDTH;
        }
    }

    // Copied from InventoryScreen because this method does not exist in 1.20.1
    @SuppressWarnings("deprecation")
    private void drawEntity (DrawContext context, float x, float y, int size, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity) {
        context.getMatrices().push();
        context.getMatrices().translate((double)x, (double)y, 50.0);
        context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float)size, (float)size, (float)(-size)));
        context.getMatrices().translate(vector3f.x, vector3f.y, vector3f.z);
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadows(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.getMatrices(), context.getVertexConsumers(), 15728880);
        });
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    private void drawHorizontallyMirroredTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        context.drawTexturedQuad(texture, x1, x2, y1, y2, z, u2, u1, v1, v2);
    }

    private void drawTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, Vector3f colour) {
        context.drawTexturedQuad(texture, x1, x2, y1, y2, z, u1, u2, v1, v2, colour.x, colour.y, colour.z, 1f);
    }

    private void drawHorizontallyMirroredTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, Vector3f colour) {
        context.drawTexturedQuad(texture, x1, x2, y1, y2, z, u2, u1, v1, v2, colour.x, colour.y, colour.z, 1f);
    }
}
