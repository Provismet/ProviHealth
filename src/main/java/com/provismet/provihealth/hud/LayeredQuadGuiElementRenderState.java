package com.provismet.provihealth.hud;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector3f;

public record LayeredQuadGuiElementRenderState (
    RenderPipeline pipeline,
    TextureSetup textureSetup,
    Matrix3x2f matrices,
    int x1,
    int y1,
    int x2,
    int y2,
    int z,
    float u1,
    float u2,
    float v1,
    float v2,
    Vector3f colour,
    @Nullable ScreenRect scissorArea,
    @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public LayeredQuadGuiElementRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f matrices,
        int x1,
        int y1,
        int x2,
        int y2,
        int z,
        float u1,
        float u2,
        float v1,
        float v2,
        Vector3f colour,
        @Nullable ScreenRect scissorArea
    ) {
        this(pipeline, textureSetup, matrices, x1, y1, x2, y2, z, u1, u2, v1, v2, colour, scissorArea, createBounds(x1, y1, x2, y2, matrices, scissorArea));
    }

    @Override
    public void setupVertices (VertexConsumer vertices) {
        // TODO: How to control layering?
        vertices.vertex(matrices, (float)x1, (float)y1).texture(u1, v1).color(colour.x, colour.y, colour.z, 1f);
        vertices.vertex(matrices, (float)x1, (float)y2).texture(u1, v2).color(colour.x, colour.y, colour.z, 1f);
        vertices.vertex(matrices, (float)x2, (float)y2).texture(u2, v2).color(colour.x, colour.y, colour.z, 1f);
        vertices.vertex(matrices, (float)x2, (float)y1).texture(u2, v1).color(colour.x, colour.y, colour.z, 1f);
    }

    @Nullable
    private static ScreenRect createBounds (int x1, int y1, int x2, int y2, Matrix3x2f matrices, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = new ScreenRect(x1, y1, x2 - x1, y2 - y1).transformEachVertex(matrices);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
