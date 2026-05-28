package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Cola de efectos visuales tardíos (late effects) para el frame actual.
 * <p>
 * Tile entity renderers y otros sistemas de renderizado pueden encolar lambdas aquí durante
 * {@code extractRenderState()} o {@code submit()}, y {@link RenderTickHandler} las drenará
 * en {@code RenderLevelStageEvent.AfterTranslucentParticles} donde se dispone de
 * {@link MultiBufferSource.BufferSource} y {@link CameraRenderState}.
 * <p>
 * Esto desacopla FX transparentes/libres del pipeline de geometría estable de 26.1.
 */
public class LateEffectQueue {

    private static final List<LateEffect> effects = new ArrayList<>();

    public static void add(LateEffect effect) {
        effects.add(effect);
    }

    public static boolean hasEffects() {
        return !effects.isEmpty();
    }

    public static void renderAndClear(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
          CameraRenderState camera, long gameTime, float partialTick) {
        if (effects.isEmpty()) {
            return;
        }
        // Copy to avoid concurrent modification if a nested effect adds more
        List<LateEffect> toRender = new ArrayList<>(effects);
        effects.clear();
        for (LateEffect effect : toRender) {
            effect.render(poseStack, bufferSource, camera, gameTime, partialTick);
        }
    }

    @FunctionalInterface
    public interface LateEffect {

        void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
              CameraRenderState camera, long gameTime, float partialTick);
    }
}
