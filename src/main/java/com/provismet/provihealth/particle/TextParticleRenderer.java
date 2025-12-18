package com.provismet.provihealth.particle;

import com.provismet.provihealth.config.Options;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class TextParticleRenderer extends ParticleRenderer<TextParticle> {
    public static final ParticleTextureSheet PARTICLE_TEXTURE_SHEET = new ParticleTextureSheet("PROVIHEALTH_TEXT");

    public TextParticleRenderer (ParticleManager particleManager) {
        super(particleManager);
    }

    @Override
    public Submittable render (Frustum frustum, Camera camera, float tickProgress) {
        return new TextParticleRenderer.Result(
            this.particles.stream().map(textParticle -> TextParticleRenderer.State.create(textParticle, camera, tickProgress)).toList()
        );
    }

    record Result (List<TextParticleRenderer.State> states) implements Submittable {
        @Override
        public void submit (OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
            for (State state : this.states) {
                queue.submitText(
                    state.matrices,
                    0, 0,
                    Text.literal(state.text).asOrderedText(),
                    Options.particleTextShadow,
                    TextRenderer.TextLayerType.POLYGON_OFFSET,
                    state.light,
                    state.colour,
                    0,
                    0
                );
            }
        }
    }

    record State (MatrixStack matrices, String text, int colour, int light) {
        public static TextParticleRenderer.State create (TextParticle particle, Camera camera, float tickDelta) {
            MatrixStack matrices = new MatrixStack();
            matrices.push();
            float dX = (float)(MathHelper.lerp(tickDelta, particle.getPrevPos().x, particle.getPos().x) - camera.getCameraPos().getX());
            float dY = (float)(MathHelper.lerp(tickDelta, particle.getPrevPos().y, particle.getPos().y) - camera.getCameraPos().getY());
            float dZ = (float)(MathHelper.lerp(tickDelta, particle.getPrevPos().z, particle.getPos().z) - camera.getCameraPos().getZ());

            matrices.translate(dX, dY, dZ);
            matrices.multiply(camera.getRotation());
            float scaleSize = particle.getSize(tickDelta) / 6f;
            matrices.scale(scaleSize, -scaleSize, scaleSize);

            return new TextParticleRenderer.State(matrices, particle.getText(), particle.getColour(), particle.getBrightness(tickDelta));
        }
    }
}
