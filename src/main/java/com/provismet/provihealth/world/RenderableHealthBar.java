package com.provismet.provihealth.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.util.ColourHelper;
import java.util.Optional;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public record RenderableHealthBar (Optional<Integer> teamColour, int index, float percentage, boolean isMount) implements SubmitNodeCollector.CustomGeometryRenderer {
    @Override
    public void render (PoseStack.Pose matrix, VertexConsumer vertexConsumer) {
        float clampedPercentage = Mth.clamp(this.percentage, 0f, 1f);
        float healthPercentage = 1 - clampedPercentage; // Just to decouple the colouring from the background image.
        if (this.index == 1) clampedPercentage = 1f;
        if (this.isMount) clampedPercentage = Mth.lerp(clampedPercentage, 3f / EntityHealthBar.TEXTURE_SIZE, 61f / EntityHealthBar.TEXTURE_SIZE);

        // As of 1.21, the rendering was changed for whatever reason and the bars were facing in the wrong direction (which makes them invisible).
        // This method now renders them backwards because simply rotating them was causing even more issues.

        // All U and V values are a percentage.
        final float MIN_U = 1f - clampedPercentage; // Leftmost pixel
        final float MIN_V = ((this.index * 12f) / EntityHealthBar.TEXTURE_SIZE) + (this.isMount ? 7f / EntityHealthBar.TEXTURE_SIZE : 0f); // Topmost pixel
        final float MAX_U = 1f; // Rightmost pixel
        final float MAX_V = MIN_V + (this.isMount ? 5f : 7f) / EntityHealthBar.TEXTURE_SIZE; // Bottommost pixel

        // X and Y are block coordinates relative to the matrix shenanigans.
        final float MAX_X = -0.5f; // Pushes the bar half a block to the left, centering it.
        final float MIN_X = MAX_X + clampedPercentage;
        final float MIN_Y = 0f;
        final float MAX_Y = -1f * ((this.isMount ? 5f : 7f) / EntityHealthBar.TEXTURE_SIZE); // Mount bar is 5 pixels tall, Health bar is 7 pixels tall.

        final float Z = (float)this.index * -0.0001f;

        int colour;
        if (!Options.tintBackground && this.index == 1) {
            colour = CommonColors.WHITE;
        }
        else if (Options.useTeamColours && this.teamColour.isPresent()) {
            colour = ARGB.opaque(this.teamColour.get());
        }
        else {
            colour = ARGB.opaque(ColourHelper.lerpBarColour(healthPercentage, Options.worldStartColour, Options.worldEndColour, Options.worldGradient));
        }

        int maxLight = 0xF000F0;
        vertexConsumer.addVertex(matrix, MIN_X, MIN_Y, Z).setUv(MIN_U, MIN_V).setLight(maxLight).setColor(colour); // Top-Left
        vertexConsumer.addVertex(matrix, MAX_X, MIN_Y, Z).setUv(MAX_U, MIN_V).setLight(maxLight).setColor(colour); // Top-Right
        vertexConsumer.addVertex(matrix, MAX_X, MAX_Y, Z).setUv(MAX_U, MAX_V).setLight(maxLight).setColor(colour); // Bottom-Right
        vertexConsumer.addVertex(matrix, MIN_X, MAX_Y, Z).setUv(MIN_U, MAX_V).setLight(maxLight).setColor(colour); // Bottom-Left
    }
}
