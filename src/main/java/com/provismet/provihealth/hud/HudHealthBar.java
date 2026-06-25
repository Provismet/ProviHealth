package com.provismet.provihealth.hud;

import com.provismet.provihealth.ProviHealthClient;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.config.Options.HUDPortraitCompatMode;
import com.provismet.provihealth.config.Options.HUDPosition;
import com.provismet.provihealth.config.Options.HUDType;
import com.provismet.provihealth.config.resources.EntityOptions;
import com.provismet.provihealth.interfaces.IMixinEntityRenderState;
import com.provismet.provihealth.interfaces.IMixinLivingEntity;
import com.provismet.provihealth.util.ColourHelper;
import com.provismet.provihealth.util.HealthCalculator;
import com.provismet.provihealth.util.HealthContainer;
import com.provismet.provihealth.util.Visibility;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class HudHealthBar implements HudElement {
    public static final Identifier HEALTHBAR_LAYER = ProviHealthClient.identifier("healthbar_layer");

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
    public void extractRenderState (GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
        if (this.healthBarDuration > 0f) this.healthBarDuration -= tickDelta;
        else this.reset();


        if (Minecraft.getInstance().gui.hud.isHidden()
            || Minecraft.getInstance().getDebugOverlay().showDebugScreen()
            || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSpectator())) return;

        boolean isNewTarget = false;

        if (Minecraft.getInstance().crosshairPickEntity instanceof LivingEntity living) {
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

        float healthPercent = Mth.clamp(this.target.getHealth() / this.target.getMaxHealth(), 0f, 1f);

        HealthContainer mountHealth = HealthCalculator.getRecursiveMountHealth(target, Options.BarType.HUD);
        float vehicleHealthPercent = mountHealth != null ? mountHealth.getPercentage() : 0f;

        int healthWidth = Math.round(BAR_WIDTH * healthPercent);
        int vehicleHealthWidth = Math.round(MOUNT_BAR_WIDTH * vehicleHealthPercent);

        if (isNewTarget) {
            this.currentHealthWidth = healthWidth;
            this.currentVehicleHealthWidth = vehicleHealthWidth;
        }

        final int nameWidth = Minecraft.getInstance().font.width(this.getName(this.target));
        if (hudType.showBars) {
            // Render bars
            Identifier healthbarTexture = entityOptions.getHealthBar(this.target);
            this.renderBar(drawContext, healthbarTexture, BAR_WIDTH, 1); // Empty space
            this.renderBar(drawContext, healthbarTexture, glideHealth(healthWidth, tickDelta * Options.hudGlide), 0); // Health
            if (mountHealth != null) {
                this.renderMountBar(drawContext, healthbarTexture, MOUNT_BAR_WIDTH, 1); // Empty space
                this.renderMountBar(drawContext, healthbarTexture, glideVehicleHealth(vehicleHealthWidth, tickDelta * Options.hudGlide), 0); // Health
            }

            // Render entity group icon
            int infoLeftX = LEFT_TEXT_X;
            ItemStack icon = entityOptions.getIcon(this.target);
            if (Options.hudPosition == HUDPosition.LEFT) {
                int expectedNameX = LEFT_TEXT_X + nameWidth + 2; // Starting point + width + 2 pixels of free space.
                if (icon != null && Options.showHudIcon) drawContext.item(icon, Math.max(BAR_X + BAR_WIDTH - 16, expectedNameX), BAR_Y - 16);
            }
            else {
                int expectedNameX = OFFSET_X - 18 - nameWidth; // Leftmost pixel of name, then left by 2 pixels, then left by 16 to make space for the icon.
                if (icon != null && Options.showHudIcon) drawContext.item(icon, Math.min(BAR_X, expectedNameX), BAR_Y - 16);
                infoLeftX = BAR_X + 3;
            }

            // Render health value and heart icons
            int offsetFromMountBar = (mountHealth != null ? MOUNT_BAR_HEIGHT : 0);
            int healthX = this.drawTextAndGetWidth(drawContext, String.format("%d/%d", Math.round(this.target.getHealth()), Math.round(this.target.getMaxHealth())), infoLeftX, TEXT_BASE_Y + 1 + offsetFromMountBar, 0xFFFFFF, true); // Health Value
            drawContext.blit(RenderPipelines.GUI_TEXTURED, HEART, healthX, TEXT_BASE_Y + offsetFromMountBar, 0f, 0f, 9, 9, 9, 9, 9, 9);

            // Render armour icon if necessary
            int armourX = Minecraft.getInstance().font.width(String.format("%d/%d", Math.round(this.target.getMaxHealth()), Math.round(this.target.getMaxHealth()))) + infoLeftX + 18;
            if (this.target.getArmorValue() > 0) {
                armourX = this.drawTextAndGetWidth(drawContext, String.format("%d", this.target.getArmorValue()), armourX, TEXT_BASE_Y + 1 + offsetFromMountBar, 0xFFFFFF, true);
                drawContext.blit(RenderPipelines.GUI_TEXTURED, ARMOUR, armourX, TEXT_BASE_Y + offsetFromMountBar, 0f, 0f, 9, 9, 9, 9, 9, 9);
            }

            // Render mount health icon/text if necessary
            if (mountHealth != null) {
                String mountHealthString = String.format("%d/%d", Math.round(mountHealth.getCurrent()), Math.round(mountHealth.getMax()));
                int mountHealthWidth = Minecraft.getInstance().font.width(mountHealthString) + 9;
                int expectedLeftPixel = BAR_X + BAR_WIDTH - mountHealthWidth - 3;

                if (expectedLeftPixel < armourX) expectedLeftPixel = armourX + 10;

                int mountHealthX = this.drawTextAndGetWidth(drawContext, mountHealthString, expectedLeftPixel, TEXT_BASE_Y + 1 + MOUNT_BAR_HEIGHT, 0xFFFFFF, true);
                drawContext.blit(RenderPipelines.GUI_TEXTURED, MOUNT_HEART, mountHealthX, TEXT_BASE_Y + MOUNT_BAR_HEIGHT, 0f, 0f, 9, 9, 9, 9, 9, 9);
            }

            if (Options.hudStatuses) {
                List<Holder<MobEffect>> effects = ((IMixinLivingEntity)this.target).provi_Health$getClientSideStatusEffects();

                if (!effects.isEmpty()) {
                    int effectXOffset = 0;
                    for (Holder<MobEffect> effect : effects) {
                        Identifier effectTexture = Hud.getMobEffectSprite(effect);
                        drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, effectTexture, EFFECT_X + effectXOffset, EFFECT_BASE_Y + offsetFromMountBar, 16, 16);
                        effectXOffset += EFFECT_X_OFFSET;
                    }
                }
            }
        }

        if (Options.hudTitles && hudType.showTitles) {
            List<Component> titles = ElementRegistry.getTitle(this.target, false, true).reversed();

            int titleX = 5;
            int titleY = OFFSET_Y + FRAME_LENGTH + 5;

            if (Options.hudPosition == HUDPosition.LEFT) {
                for (Component title : titles) {
                    drawContext.text(Minecraft.getInstance().font, title, titleX, titleY, CommonColors.WHITE, true);
                    titleY += 10;
                }
            }
            else {
                for (Component title : titles) {
                    titleX = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 10 - Minecraft.getInstance().font.width(title);
                    drawContext.text(Minecraft.getInstance().font, title, titleX, titleY, CommonColors.WHITE, true);
                    titleY += 10;
                }
            }
        }

        if (hudType.showPortrait) {
            // Render Portrait Background and Text (foreground comes later)
            if (Options.hudPosition == HUDPosition.LEFT) {
                this.drawTexturedWhiteQuad(entityOptions.getBorder(this.target), drawContext, 0, OFFSET_Y, 48f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH); // Background
                drawContext.text(Minecraft.getInstance().font, this.getName(this.target), LEFT_TEXT_X, BAR_Y - BAR_HEIGHT, CommonColors.WHITE, true); // Name
            }
            else {
                this.drawHorizontallyMirroredTexturedQuad(entityOptions.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, 0.5f, 1f, 0f, 1f, CommonColors.WHITE); // Background
                drawContext.text(Minecraft.getInstance().font, this.getName(this.target), OFFSET_X - 1 - nameWidth, BAR_Y - BAR_HEIGHT, CommonColors.WHITE, true); // Name
            }

            // Render Paper Doll
            if (Options.HUDCompat == HUDPortraitCompatMode.STANDARD) {
                float prevTargetHeadYaw = this.target.yHeadRot;
                float prevPrevTargetHeadYaw = this.target.yHeadRotO;
                float prevTargetBodyYaw = this.target.yBodyRot;
                float prevPrevTargetBodyYaw = this.target.yBodyRotO;

                this.target.yBodyRot = Options.hudPosition.portraitYAW;
                this.target.yBodyRotO = Options.hudPosition.portraitYAW;
                this.target.yHeadRot = Options.hudPosition.portraitYAW;
                this.target.yHeadRotO = Options.hudPosition.portraitYAW;

                this.drawEntity(drawContext, (new Quaternionf()).rotateZ(3.1415927f));

                this.target.yHeadRot = prevTargetHeadYaw;
                this.target.yHeadRotO = prevPrevTargetHeadYaw;
                this.target.yBodyRot = prevTargetBodyYaw;
                this.target.yBodyRotO = prevPrevTargetBodyYaw;
            }
            else if (Options.HUDCompat == HUDPortraitCompatMode.COMPAT) {
                float yawOffset = -(Options.hudPosition.portraitYAW - this.target.getVisualRotationYInDegrees()) / Mth.RAD_TO_DEG;
                this.drawEntity(drawContext, (new Quaternionf()).rotateZ(3.1415927f).rotateY(yawOffset));
            }

            // Draw portrait background.
            if (Options.hudPosition == HUDPosition.LEFT) {
                this.drawTexturedWhiteQuad(entityOptions.getBorder(this.target), drawContext, 0, OFFSET_Y, 0f, 0f, FRAME_LENGTH, FRAME_LENGTH, FRAME_LENGTH * 2, FRAME_LENGTH);
            }
            else {
                this.drawHorizontallyMirroredTexturedQuad(entityOptions.getBorder(this.target), drawContext, OFFSET_X, OFFSET_X + FRAME_LENGTH, OFFSET_Y, OFFSET_Y + FRAME_LENGTH, 0f, 0.5f, 0f, 1f, CommonColors.WHITE); // Foreground
            }
        }
    }

    private Component getName (LivingEntity entity) {
        if (entity instanceof Player && entity.isInvisibleTo(Minecraft.getInstance().player)) return Component.translatable("entity.provihealth.unknownPlayer");
        else return entity.getDisplayName();
    }

    private int glideHealth (int trueValue, float glideFactor) {
        this.currentHealthWidth += (int)((float)(trueValue - this.currentHealthWidth) * Mth.clamp(glideFactor, 0.001f, 1f));
        return this.currentHealthWidth;
    }

    private int glideVehicleHealth (int trueValue, float glideFactor) {
        this.currentVehicleHealthWidth += (int)((float)(trueValue - this.currentVehicleHealthWidth) * Mth.clamp(glideFactor, 0.001f, 1f));
        return this.currentVehicleHealthWidth;
    }

    private void renderBar (GuiGraphicsExtractor drawContext, Identifier texture, int width, int barIndex) {
        int barColour = ARGB.opaque(ColourHelper.lerpBarColour(1 - (float)width / (float)BAR_WIDTH, barIndex == 1 ? CommonColors.WHITE : Options.hudStartColour, Options.hudEndColour, barIndex == 0 && Options.hudGradient));
        if (Options.hudPosition == HUDPosition.LEFT) drawContext.innerBlit(RenderPipelines.GUI_TEXTURED, texture, BAR_X, BAR_X + width, BAR_Y, BAR_Y + BAR_HEIGHT, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, barColour);
        else this.drawHorizontallyMirroredTexturedQuad(texture, drawContext, BAR_X + (BAR_WIDTH - width), BAR_X + BAR_WIDTH, BAR_Y, BAR_Y + BAR_HEIGHT, 0f, (float)width / (float)BAR_WIDTH, barIndex / 2f, BAR_V2 + barIndex / 2f, barColour);
    }

    private void renderMountBar (GuiGraphicsExtractor drawContext, Identifier texture, int width, int barIndex) {
        int barColour = ARGB.opaque(ColourHelper.lerpBarColour(1 - (float)width / (float)MOUNT_BAR_WIDTH, barIndex == 1 ? CommonColors.WHITE : Options.hudStartColour, Options.hudEndColour, barIndex == 0 && Options.hudGradient));
        if (Options.hudPosition == HUDPosition.LEFT) drawContext.innerBlit(RenderPipelines.GUI_TEXTURED, texture, BAR_X, BAR_X + width, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, barColour);
        else this.drawHorizontallyMirroredTexturedQuad(texture, drawContext, BAR_X + (MOUNT_BAR_WIDTH - width) + BAR_WIDTH_DIFF, BAR_X + BAR_WIDTH_DIFF + MOUNT_BAR_WIDTH, BAR_Y + BAR_HEIGHT, BAR_Y + BAR_HEIGHT + MOUNT_BAR_HEIGHT, 0f, ((float)width / (float)MOUNT_BAR_WIDTH) * MOUNT_BAR_U2, MOUNT_BAR_V1 + barIndex / 2f, MOUNT_BAR_V2 + barIndex / 2f, barColour);
    }

    private void reset () {
        this.healthBarDuration = 0f;
        this.target = null;
        this.currentHealthWidth = 0;
        this.currentVehicleHealthWidth = 0;
    }

    private void adjustForScreenSize () {
        OFFSET_Y = Math.min((int)(Minecraft.getInstance().getWindow().getGuiScaledHeight() * (Options.hudOffsetPercent / 100f)), Minecraft.getInstance().getWindow().getGuiScaledHeight() - FRAME_LENGTH);
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
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            OFFSET_X = width - FRAME_LENGTH;
            BAR_X = OFFSET_X + 5 - BAR_WIDTH;
            EFFECT_X = OFFSET_X - 18;
            EFFECT_X_OFFSET = -17;
        }
    }

    private void drawHorizontallyMirroredTexturedQuad (Identifier texture, GuiGraphicsExtractor context, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int colour) {
        this.drawTexturedQuad(texture, context, x1, x2, y1, y2, u2, u1, v1, v2, colour);
    }

    private void drawTexturedWhiteQuad (Identifier texture, GuiGraphicsExtractor context, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexturedQuad(texture, context, x, x + width, y, y + height, u / (float)textureWidth, (u + (float)width) / (float)textureWidth, v / (float)textureHeight, (v + (float)height) / (float)textureHeight, CommonColors.WHITE);
    }

    /**
     * Recreation of DrawContext#drawTexturedQuad that allows for changing the layering order (z-axis).
     *
     * @param texture The location of the texture to draw.
     * @param context The DrawContext to get rendering data from.
     * @param x1 The screen-coordinate of the leftmost pixel.
     * @param x2 The screen-coordinate of the rightmost pixel.
     * @param y1 The screen-coordinate of the topmost pixel.
     * @param y2 The screen-coordinate of the bottommost pixel.
     * @param u1 As a percentage of the texture-width, the leftmost pixel to read and render.
     * @param u2 As a percentage of the texture-width, the rightmost pixel to read and render.
     * @param v1 As a percentage of the texture-height, the topmost pixel to read and render.
     * @param v2 As a percentage of the texture-height, the bottommost pixel to read and render.
     * @param colour Colour
     */
    private void drawTexturedQuad (Identifier texture, GuiGraphicsExtractor context, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int colour) {
        AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
        context.guiRenderState.addGuiElement(
            new BlitRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler()),
                context.pose(),
                x1, y1,
                x2, y2,
                u1, u2,
                v1, v2,
                colour,
                context.scissorStack.peek()
            )
        );
    }

    private int drawTextAndGetWidth (GuiGraphicsExtractor context, String text, int x, int y, int colour, boolean shadow) {
        context.text(Minecraft.getInstance().font, text, x, y, ARGB.opaque(colour), shadow);
        return x + Minecraft.getInstance().font.width(text);
    }

    private void drawEntity (GuiGraphicsExtractor context, Quaternionf rotation) {
        float renderHeight;
        if (this.target.getEyeHeight(Pose.STANDING) >= this.target.getBbHeight() * 0.6) {
            renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.5f;
            if (renderHeight < 1f) renderHeight = 1f;
        }
        else renderHeight = this.target.getEyeHeight(this.target.getPose()) + 0.8f;

        context.enableScissor(OFFSET_X, OFFSET_Y, OFFSET_X + FRAME_LENGTH, OFFSET_Y + FRAME_LENGTH);
        this.drawEntity(
            context,
            OFFSET_X,
            OFFSET_Y - 50,
            OFFSET_X + FRAME_LENGTH,
            OFFSET_Y + FRAME_LENGTH,
            30,
            new Vector3f(0f, renderHeight, 0f),
            rotation,
            this.target
        );
        context.disableScissor();
    }

    private void drawEntity (
        GuiGraphicsExtractor drawer,
        int x1,
        int y1,
        int x2,
        int y2,
        float scale,
        Vector3f translation,
        Quaternionf rotation,
        LivingEntity entity
    ) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState state = entityRenderer.createRenderState(entity, 1.0F);
        state.displayFireAnimation = false;
        state.nameTagAttachment = null;
        state.outlineColor = 0;
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        ((IMixinEntityRenderState)state).provi_Health$setShouldRenderHealth(false);
        drawer.entity(state, scale, translation, rotation, null, x1, y1, x2, y2);
    }
}
