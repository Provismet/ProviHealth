package com.provismet.provihealth.world;

import com.provismet.provihealth.config.Options;
import com.provismet.provihealth.util.ColourHelper;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public record RenderableHealthBar (Optional<Integer> teamColour, int index, float percentage, boolean isMount) implements OrderedRenderCommandQueue.Custom {
    @Override
    public void render (MatrixStack.Entry matrix, VertexConsumer vertexConsumer) {
        float clampedPercentage = MathHelper.clamp(this.percentage, 0f, 1f);
        float healthPercentage = clampedPercentage; // Just to decouple the colouring from the background image.
        if (this.index == 1) clampedPercentage = 1f;
        if (this.isMount) clampedPercentage = MathHelper.lerp(clampedPercentage, 3f / EntityHealthBar.TEXTURE_SIZE, 61f / EntityHealthBar.TEXTURE_SIZE);

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
            colour = Colors.WHITE;
        }
        else if (Options.useTeamColours && this.teamColour.isPresent()) {
            colour = ColorHelper.fullAlpha(this.teamColour.get());
        }
        else {
            colour = ColorHelper.fullAlpha(ColourHelper.lerpBarColour(healthPercentage, Options.worldStartColour, Options.worldEndColour, Options.worldGradient));
        }

        int maxLight = 0xF000F0;
        vertexConsumer.vertex(matrix, MIN_X, MIN_Y, Z).texture(MIN_U, MIN_V).light(maxLight).color(colour); // Top-Left
        vertexConsumer.vertex(matrix, MAX_X, MIN_Y, Z).texture(MAX_U, MIN_V).light(maxLight).color(colour); // Top-Right
        vertexConsumer.vertex(matrix, MAX_X, MAX_Y, Z).texture(MAX_U, MAX_V).light(maxLight).color(colour); // Bottom-Right
        vertexConsumer.vertex(matrix, MIN_X, MAX_Y, Z).texture(MIN_U, MAX_V).light(maxLight).color(colour); // Bottom-Left
    }
}
