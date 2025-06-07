package com.provismet.provihealth.hud;

import com.provismet.provihealth.config.resources.EntityOptions;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.util.FunctionalUtilities;
import com.provismet.provihealth.util.HealthCalculator;
import com.provismet.provihealth.util.HealthContainer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.HUDPortraitCompatMode;
import com.provismet.provihealth.config.Options.HUDPosition;
import com.provismet.provihealth.config.Options.HUDType;
import com.provismet.provihealth.util.Visibility;
import com.provismet.provihealth.world.EntityHealthBar;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Function;

public class TargetHealthBar implements HudLayerRegistrationCallback, LayeredDrawer.Layer {
    public static final Identifier HEALTHBAR_LAYER = ProviHealthClient.identifier("healthbar_layer");

    public static boolean disabledLabels = false;

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
    private static final int FOREGROUND_Z = 300;
    private static final int BACKGROUND_Z = 0;

    private static int TEXT_BASE_Y = BAR_Y + BAR_HEIGHT + 1;

    private static int EFFECT_X = FRAME_LENGTH + 2;
    private static int EFFECT_BASE_Y = TEXT_BASE_Y + 11;
    private static int EFFECT_X_OFFSET = 17;

    private static final float BAR_V2 = ((float)BAR_HEIGHT / (float)(BAR_HEIGHT + MOUNT_BAR_HEIGHT)) / 2f; // Accounting for index.
    private static final float MOUNT_BAR_U2 = (float)MOUNT_BAR_WIDTH / (float)BAR_WIDTH;
    private static final float MOUNT_BAR_V1 = ((float)BAR_HEIGHT / (float)(BAR_HEIGHT + MOUNT_BAR_HEIGHT)) / 2f;
    private static final float MOUNT_BAR_V2 = 0.5f; // Accounting for index.

    private static final int BAR_WIDTH_DIFF = BAR_WIDTH - MOUNT_BAR_WIDTH;

    private LivingEntity target = null;
    private float healthBarDuration = 0f;

    private int currentHealthWidth;
    private int currentVehicleHealthWidth;

    @Override
    public void render (DrawContext drawContext, RenderTickCounter tickCounter) {
        float tickDelta = tickCounter.getTickProgress(true);
        if (this.healthBarDuration > 0f) this.healthBarDuration -= tickDelta;
        else this.reset();

        if (!MinecraftClient.isHudEnabled()
            || MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud()
            || (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isSpectator())) return;

        boolean isNewTarget = false;

        if (MinecraftClient.getInstance().targetedEntity instanceof LivingEntity living) {
            if (!Visibility.isVisible(living)) return;
            if (!living.equals(this.target)) isNewTarget = true;
            this.target = living;
            this.healthBarDuration = Options.maxHealthBarTicks;
        }

        if (this.healthBarDuration <= 0f) return;
        if (this.target == null) {
            this.reset();
            return;
        }

        this.adjustForScreenSize();
        EntityOptions entityOptions = ElementRegistry.getEntityOptions(this.target);
        HUDType hudType = entityOptions.getHudType(this.target);

        float healthPercent = MathHelper.clamp(this.target.getHealth() / this.target.getMaxHealth(), 0f, 1f);

        HealthContainer mountHealth = HealthCalculator.getRecursiveMountHealth(target, Options.BarType.HUD);
        float vehicleHealthPercent = mountHealth != null ? mountHealth.getPercentage() : 0f;

        int healthWidth = Math.round(BAR_WIDTH * healthPercent);
        int vehicleHealthWidth = Math.round(MOUNT_BAR_WIDTH * vehicleHealthPercent);

        if (isNewTarget) {
            this.currentHealthWidth = healthWidth;
            this.currentVehicleHealthWidth = vehicleHealthWidth;
        }

        final int nameWidth = MinecraftClient.getInstance().textRenderer.getWidth(this.getName(this.target));
        if (hudType == HUDType.FULL) {
            // Render bars
            Identifier healthbarTexture = entityOptions.getHealthBar(this.target);
            this.renderBar(drawContext, healthbarTexture, BAR_WIDTH, 1); // Empty space
            this.renderBar(drawContext, healthbarTexture, glideHealth(healthWidth, tickDelta * Options.hudGlide), 0); // Health
            if (mountHealth != null) {
                this.renderMountBar(drawContext, healthbarTexture, MOUNT_BAR_WIDTH, 1); // Empty space
                this.renderMountBar(drawContext, healthbarTexture, glideVehicleHealth(vehicleHealthWidth, tickDelta * Options.hudGlide), 0); // Health
            }

            int infoLeftX = LEFT_TEXT_X;
            ItemStack icon = entityOptions.getIcon(this.target);
            if (Options.hudPosition == HUDPosition.LEFT) {
                // Render entity group icon
                int expectedNameX = LEFT_TEXT_X + nameWidth + 2; // Starting point + width + 2 pixels of free space.
                if (icon != null && Options.showHudIcon) drawContext.drawItem(icon, Math.max(BAR_X + BAR_WIDTH - 16, expectedNameX), BAR_Y - 16);
            }
            else {
                int expectedNameX = OFFSET_X - 18 - nameWidth; // Leftmost pixel of name, then left by 2 pixels, then left by 16 to make space for the icon.
                if (icon != null && Options.showHudIcon) drawContext.drawItem(icon, Math.min(BAR_X, expectedNameX), BAR_Y - 16);
                infoLeftX = BAR_X + 3;
            }

            // Render health value and heart icons
            int offsetFromMountBar = (mountHealth != null ? MOUNT_BAR_HEIGHT : 0);
            int healthX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d/%d", Math.round(this.target.getHealth()), Math.round(this.target.getMaxHealth())), infoLeftX, TEXT_BASE_Y + 1 + offsetFromMountBar, 0xFFFFFF, true); // Health Value
            drawContext.drawTexture(RenderLayer::getGuiTextured, HEART, healthX, TEXT_BASE_Y + offsetFromMountBar, 0f, 0f, 9, 9, 9, 9, 9, 9);

            // Render armour icon if necessary
            int armourX = MinecraftClient.getInstance().textRenderer.getWidth(String.format("%d/%d", Math.round(this.target.getMaxHealth()), Math.round(this.target.getMaxHealth()))) + infoLeftX + 18;
            if (this.target.getArmor() > 0) {
                armourX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d", this.target.getArmor()), armourX, TEXT_BASE_Y + 1 + offsetFromMountBar, 0xFFFFFF, true);
                drawContext.drawTexture(RenderLayer::getGuiTextured, ARMOUR, armourX, TEXT_BASE_Y + offsetFromMountBar, 0f, 0f, 9, 9, 9, 9, 9, 9);
            }

            if (mountHealth != null) {
                String mountHealthString = String.format("%d/%d", Math.round(mountHealth.getCurrent()), Math.round(mountHealth.getMax()));
                int mountHealthWidth = MinecraftClient.getInstance().textRenderer.getWidth(mountHealthString) + 9;
                int expectedLeftPixel = BAR_X + BAR_WIDTH - mountHealthWidth - 3;

                if (expectedLeftPixel < armourX) expectedLeftPixel = armourX + 10;

                int mountHealthX = drawContext.drawText(MinecraftClient.getInstance().textRenderer, mountHealthString, expectedLeftPixel, TEXT_BASE_Y + 1 + MOUNT_BAR_HEIGHT, 0xFFFFFF, true);
                drawContext.drawTexture(RenderLayer::getGuiTextured, MOUNT_HEART, mountHealthX, TEXT_BASE_Y + MOUNT_BAR_HEIGHT, 0f, 0f, 9, 9, 9, 9, 9, 9);
            }

            if (Options.hudStatuses) {
                List<RegistryEntry<StatusEffect>> effects = ((IMixinLivingEntity)this.target).provi_Health$getClientSideStatusEffects();

                if (!effects.isEmpty()) {
                    StatusEffectSpriteManager statusEffectSpriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
                    int effectXOffset = 0;
                    for (RegistryEntry<StatusEffect> effect : effects) {
                        Sprite effectSprite = statusEffectSpriteManager.getSprite(effect);
                        drawContext.drawSpriteStretched(RenderLayer::getGuiTextured, effectSprite, EFFECT_X + effectXOffset, EFFECT_BASE_Y + offsetFromMountBar, 16, 16);
                        effectXOffset += EFFECT_X_OFFSET;
                    }
                }
            }

            // Render titles on HUD
            if (Options.hudTitles) {
                List<Text> titles = ElementRegistry.getTitle(this.target, false, true).reversed();

                int titleX = 5;
                int titleY = OFFSET_Y + FRAME_LENGTH + 5;

                if (Options.hudPosition == HUDPosition.LEFT) {
                    for (Text title : titles) {
                        drawContext.drawText(MinecraftClient.getInstance().textRenderer, title, titleX, titleY, 0xFFFFFF, true);
                        titleY += 10;
                    }
                }
                else {
                    for (Text title : titles) {
                        titleX = MinecraftClient.getInstance().getWindow().getScaledWidth() - 10 - MinecraftClient.getInstance().textRenderer.getWidth(title);
                        drawContext.drawText(MinecraftClient.getInstance().textRenderer, title, titleX, titleY, 0xFFFFFF, true);
                        titleY += 10;
                    }
                }
            }
        }

        if (hudType != HUDType.NONE) {
            // Render Portrait
            if (Options.hudPosition == HUDPosition.LEFT) {
                this.drawTexturedQuad(entityOptions.getBorder(this.target), drawContext, 0, OFFSET_Y, BACKGROUND_Z, 48f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Background
                this.drawTexturedQuad(entityOptions.getBorder(this.target), drawContext, 0, OFFSET_Y, FOREGROUND_Z, 0f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Foreground

                drawContext.drawText(MinecraftClient.getInstance().textRenderer, this.getName(this.target), LEFT_TEXT_X, BAR_Y - BAR_HEIGHT, 0xFFFFFF, true); // Name
            }
            else {
                this.drawHorizontallyMirroredTexturedQuad(entityOptions.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, BACKGROUND_Z, 0.5f, 1f, 0f, 1f); // Background
                this.drawHorizontallyMirroredTexturedQuad(entityOptions.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, FOREGROUND_Z, 0f, 0.5f, 0f, 1f); // Foreground

                drawContext.drawText(MinecraftClient.getInstance().textRenderer, this.getName(this.target), OFFSET_X - 1 - nameWidth, BAR_Y - BAR_HEIGHT, 0xFFFFFF, true); // Name
            }

            // Render Paper Doll
            if (Options.HUDCompat == HUDPortraitCompatMode.STANDARD) {
                float prevTargetHeadYaw = this.target.headYaw;
                float prevPrevTargetHeadYaw = this.target.lastHeadYaw;
                float prevTargetBodyYaw = this.target.bodyYaw;
                float prevPrevTargetBodyYaw = this.target.lastBodyYaw;

                this.target.bodyYaw = Options.hudPosition.portraitYAW;
                this.target.lastBodyYaw = Options.hudPosition.portraitYAW;
                this.target.headYaw = Options.hudPosition.portraitYAW;
                this.target.lastHeadYaw = Options.hudPosition.portraitYAW;

                float renderHeight;
                if (this.target.getEyeHeight(EntityPose.STANDING) >= this.target.getHeight() * 0.6) {
                    renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.5f;
                    if (renderHeight < 1f) renderHeight = 1f;
                }
                else renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.8f;

                drawContext.enableScissor(OFFSET_X, OFFSET_Y, OFFSET_X + FRAME_LENGTH, OFFSET_Y + FRAME_LENGTH);
                EntityHealthBar.enabled = false;
                disabledLabels = true;
                InventoryScreen.drawEntity(
                    drawContext,
                    24 + OFFSET_X, OFFSET_Y,
                    30f,
                    new Vector3f(0f, renderHeight, 0f),
                    (new Quaternionf()).rotateZ(3.1415927f),
                    null,
                    this.target
                );
                EntityHealthBar.enabled = true;
                disabledLabels = false;
                drawContext.disableScissor();

                this.target.headYaw = prevTargetHeadYaw;
                this.target.lastHeadYaw = prevPrevTargetHeadYaw;
                this.target.bodyYaw = prevTargetBodyYaw;
                this.target.lastBodyYaw = prevPrevTargetBodyYaw;
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
                InventoryScreen.drawEntity(
                    drawContext,
                    24 + OFFSET_X,
                    OFFSET_Y,
                    30,
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

    private Text getName (LivingEntity entity) {
        if (entity instanceof PlayerEntity && entity.isInvisibleTo(MinecraftClient.getInstance().player)) return Text.translatable("entity.provihealth.unknownPlayer");
        else return entity.getDisplayName();
    }

    private int glideHealth (int trueValue, float glideFactor) {
        this.currentHealthWidth += (int)((float)(trueValue - this.currentHealthWidth) * MathHelper.clamp(glideFactor, 0.001f, 1f));
        return this.currentHealthWidth;
    }

    private int glideVehicleHealth (int trueValue, float glideFactor) {
        this.currentVehicleHealthWidth += (int)((float)(trueValue - this.currentVehicleHealthWidth) * MathHelper.clamp(glideFactor, 0.001f, 1f));
        return this.currentVehicleHealthWidth;
    }
    
    private void renderBar (DrawContext drawContext, Identifier texture, int width, int barIndex) {
        
        Vector3f startColour;
        LivingEntity entity = this.target;

        // System.out.println(((IMixinLivingEntity)this.target).provi_Health$getAnger());

        // CommandManager commandManager = Objects.requireNonNull(player.getServer()).getCommandManager();
        //     ServerCommandSource commandSource = player.getServer().getCommandSource();
        //     commandManager.executeWithPrefix(commandSource, command);


        if (barIndex == 1) startColour = Options.WHITE;

        else startColour = FunctionalUtilities.deduceColour(entity, true);

        Vector3f barColour = Options.lerpBarColour((float)width / (float)BAR_WIDTH, startColour, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient);
        if (Options.hudPosition == HUDPosition.LEFT) this.drawTexturedQuad(texture, drawContext, BAR_X, BAR_X + width, BAR_Y, BAR_Y + BAR_HEIGHT, 0, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, barColour);
        else this.drawHorizontallyMirroredTexturedQuad(texture, drawContext, BAR_X + (BAR_WIDTH - width), BAR_X + BAR_WIDTH, BAR_Y, BAR_Y + BAR_HEIGHT, 0, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, barColour);
    }

    private void renderMountBar (DrawContext drawContext, Identifier texture, int width, int barIndex) {
        Vector3f barColour = Options.lerpBarColour((float)width / (float)MOUNT_BAR_WIDTH, barIndex == 1 ? Options.WHITE : Options.unpackedDefaultStartHud, Options.unpackedEndHud, barIndex == 0 && Options.hudGradient);
        if (Options.hudPosition == HUDPosition.LEFT) this.drawTexturedQuad(texture, drawContext, BAR_X, BAR_X + width, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, barColour);
        else this.drawHorizontallyMirroredTexturedQuad(texture, drawContext, BAR_X + (MOUNT_BAR_WIDTH - width) + BAR_WIDTH_DIFF, BAR_X + BAR_WIDTH_DIFF + MOUNT_BAR_WIDTH, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, barColour);

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
        TEXT_BASE_Y = BAR_Y + BAR_HEIGHT + 1;
        EFFECT_BASE_Y = TEXT_BASE_Y + 11;

        if (Options.hudPosition == HUDPosition.LEFT) {
            OFFSET_X = 0;
            BAR_X = FRAME_LENGTH - 5;
            EFFECT_X = FRAME_LENGTH + 2;
            EFFECT_X_OFFSET = 17;
        }
        else {
            int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
            OFFSET_X = width - FRAME_LENGTH;
            BAR_X = OFFSET_X + 5 - BAR_WIDTH;
            EFFECT_X = OFFSET_X - 18;
            EFFECT_X_OFFSET = -17;
        }
    }

    private void drawHorizontallyMirroredTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        this.drawTexturedQuad(texture, context, x1, x2, y1, y2, z, u2, u1, v1, v2, Options.WHITE);
    }

    private void drawHorizontallyMirroredTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, Vector3f colour) {
        this.drawTexturedQuad(texture, context, x1, x2, y1, y2, z, u2, u1, v1, v2, colour);
    }

    private void drawTexturedQuad (Identifier texture, DrawContext context, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexturedQuad(texture, context, x, x + width, y, y + height, z, u / (float)textureWidth, (u + (float)width) / (float)textureWidth, v / (float)textureHeight, (v + (float)height) / (float)textureHeight, Options.WHITE);
    }

    /**
     * Recreation of {@link DrawContext#drawTexturedQuad(Function, Identifier, int, int, int, int, float, float, float, float, int)}
     * that allows for changing the layering order (z-axis).
     *
     * @param texture
     * @param context The DrawContext to get rendering data from.
     * @param x1 The screen-coordinate of the leftmost pixel.
     * @param x2 The screen-coordinate of the rightmost pixel.
     * @param y1 The screen-coordinate of the topmost pixel.
     * @param y2 The screen-coordinate of the bottommost pixel.
     * @param z The z-axis, higher values will be over lower values.
     * @param u1 As a percentage of the texture-width, the leftmost pixel to read and render.
     * @param u2 As a percentage of the texture-width, the rightmost pixel to read and render.
     * @param v1 As a percentage of the texture-height, the topmost pixel to read and render.
     * @param v2 As a percentage of the texture-height, the bottommost pixel to read and render.
     * @param colour Colour expressed as a vector. See {@link Vec3d#unpackRgb(int)}
     */
    private void drawTexturedQuad (Identifier texture, DrawContext context, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, Vector3f colour) {
        RenderLayer renderLayer = RenderLayer.getGuiTextured(texture);
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vertexConsumer = context.vertexConsumers.getBuffer(renderLayer);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, z).texture(u1, v1).color(colour.x, colour.y, colour.z, 1f);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, z).texture(u1, v2).color(colour.x, colour.y, colour.z, 1f);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, z).texture(u2, v2).color(colour.x, colour.y, colour.z, 1f);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, z).texture(u2, v1).color(colour.x, colour.y, colour.z, 1f);
    }

    @Override
    public void register (LayeredDrawerWrapper layeredDrawerWrapper) {
        layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.EXPERIENCE_LEVEL, TargetHealthBar.HEALTHBAR_LAYER, this);
    }
}
