package mekanism.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import mekanism.client.render.lib.effect.LaserFeatureRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;

public class LaserParticleGroup extends ParticleGroup<LaserParticle> {

    private static final float RADIAN_45 = 45 * Mth.DEG_TO_RAD;

    public static final ParticleRenderType TYPE = new ParticleRenderType("MEKANISM_LASER", "MEK_L");

    public LaserParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        return new LaserParticleGroup.State(this.particles.stream()
              //Ensure the laser is in the view, otherwise we don't need to bother rendering it
              .filter(particle -> frustum.isVisible(particle.getBoundingBox()))
              .<LaserFeatureRenderer.Submit>mapMulti((particle, consumer) -> {
                  PoseStack poseStack = new PoseStack();
                  Vec3 shift = particle.getPos().subtract(camera.position());
                  poseStack.translate(shift.x, shift.y, shift.z);
                  poseStack.rotate(particle.direction().getRotation());

                  poseStack.rotate(Axis.YP, RADIAN_45);
                  consumer.accept(new LaserFeatureRenderer.Submit(poseStack, particle));

                  poseStack.pushPose();
                  poseStack.rotate(Axis.YP, Mth.HALF_PI);
                  consumer.accept(new LaserFeatureRenderer.Submit(poseStack, particle));
              }).toList()
        );
    }

    private record State(List<LaserFeatureRenderer.Submit> states) implements ParticleGroupRenderState {

        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
            for (LaserFeatureRenderer.Submit state : this.states) {
                //Translucent particles happen during after terrain
                submitNodeCollector.submitSpecial(RenderPhaseKeys.AFTER_TERRAIN, state);
            }
        }
    }
}
