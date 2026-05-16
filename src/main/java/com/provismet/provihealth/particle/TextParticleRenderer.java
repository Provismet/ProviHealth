package com.provismet.provihealth.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.provismet.provihealth.config.Options;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextParticleRenderer extends ParticleGroup<TextParticle> {
    public static final ParticleRenderType PARTICLE_TEXTURE_SHEET = new ParticleRenderType("PROVIHEALTH_TEXT");

    public TextParticleRenderer (ParticleEngine particleManager) {
        super(particleManager);
    }

    @Override
    public ParticleGroupRenderState extractRenderState (Frustum frustum, Camera camera, float tickProgress) {
        return new TextParticleRenderer.Result(
            this.particles.stream().map(textParticle -> TextParticleRenderer.State.create(textParticle, camera, tickProgress)).toList()
        );
    }

    record Result (List<TextParticleRenderer.State> states) implements ParticleGroupRenderState {
        @Override
        public void submit (SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
            for (State state : this.states) {
                queue.submitText(
                    state.matrices,
                    0, 0,
                    Component.literal(state.text).getVisualOrderText(),
                    Options.particleTextShadow,
                    Font.DisplayMode.POLYGON_OFFSET,
                    state.light,
                    state.colour,
                    0,
                    0
                );
            }
        }
    }

    record State (PoseStack matrices, String text, int colour, int light) {
        public static TextParticleRenderer.State create (TextParticle particle, Camera camera, float tickDelta) {
            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            float dX = (float)(Mth.lerp(tickDelta, particle.getPrevPos().x, particle.getPos().x) - camera.position().x());
            float dY = (float)(Mth.lerp(tickDelta, particle.getPrevPos().y, particle.getPos().y) - camera.position().y());
            float dZ = (float)(Mth.lerp(tickDelta, particle.getPrevPos().z, particle.getPos().z) - camera.position().z());

            matrices.translate(dX, dY, dZ);
            matrices.mulPose(camera.rotation());
            float scaleSize = particle.getSize(tickDelta) / 6f;
            matrices.scale(scaleSize, -scaleSize, scaleSize);

            return new TextParticleRenderer.State(matrices, particle.getText(), particle.getColour(), particle.getLightCoords(tickDelta));
        }
    }
}
