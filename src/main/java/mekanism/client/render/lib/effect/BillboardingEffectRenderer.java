package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Supplier;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.LazyRender;
import mekanism.common.lib.effect.CustomEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    public static void render(CustomEffect effect, String profilerSection) {
        render(effect.getTexture(), profilerSection, () -> effect);
    }

    public static void render(ResourceLocation texture, String profilerSection, Supplier<CustomEffect> lazyEffect) {
        RenderType renderType = MekanismRenderType.SPS.apply(texture);
        RenderTickHandler.addTransparentRenderer(new LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer renderer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler) {
                BillboardingEffectRenderer.render(camera, renderer, poseStack, renderTick, partialTick, lazyEffect.get());
            }

            @Override
            @NotNull
            public Vec3 getCenterPos(float partialTick) {
                return lazyEffect.get().getPos(partialTick);
            }

            @Override
            @NotNull
            public String getProfilerSection() {
                return profilerSection;
            }

            @Override
            public @NotNull RenderType getRenderType() {
                return renderType;
            }
        });
    }

    private static void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, CustomEffect effect) {
        int gridSize = effect.getTextureGridSize();

        int tick = renderTick % (gridSize * gridSize);
        int xIndex = tick % gridSize, yIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;
        Quaternionf quaternion = camera.rotation();
        Vector3f[] vertexPos = {new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vec3 pos = effect.getPos(partialTick).subtract(camera.getPosition());
        for (Vector3f vector3f : vertexPos) {
            vector3f.rotate(quaternion);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }

        int[] color = effect.getColor().rgbaArray();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z())
              .setUv(minU, maxV)
              .setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z())
              .setUv(maxU, maxV)
              .setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z())
              .setUv(maxU, minV)
              .setColor(color[0], color[1], color[2], color[3]);
        buffer.addVertex(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z())
              .setUv(minU, minV)
              .setColor(color[0], color[1], color[2], color[3]);
        poseStack.popPose();
    }
}