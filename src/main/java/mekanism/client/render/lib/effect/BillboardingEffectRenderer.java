package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.function.Supplier;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.RenderTickHandler.LazyRender;
import mekanism.common.lib.effect.CustomEffect;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

public class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    public static void render(CustomEffect effect, String profilerSection) {
        render(effect.getTexture(), profilerSection, () -> effect);
    }

    public static void render(ResourceLocation texture, String profilerSection, Supplier<CustomEffect> lazyEffect) {
        RenderTickHandler.addTransparentRenderer(MekanismRenderType.SPS.apply(texture), new LazyRender() {
            @Override
            public void render(Camera camera, VertexConsumer renderer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler) {
                BillboardingEffectRenderer.render(camera, renderer, poseStack, renderTick, partialTick, lazyEffect.get());
            }

            @Override
            public Vec3 getCenterPos(float partialTick) {
                return lazyEffect.get().getPos(partialTick);
            }

            @Override
            public String getProfilerSection() {
                return profilerSection;
            }
        });
    }

    private static void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, CustomEffect effect) {
        int gridSize = effect.getTextureGridSize();

        int tick = renderTick % (gridSize * gridSize);
        int xIndex = tick % gridSize, yIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;
        Quaternion quaternion = camera.rotation();
        Vector3f[] vertexPos = {new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vec3 pos = effect.getPos(partialTick);
        for (Vector3f vector3f : vertexPos) {
            vector3f.transform(quaternion);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }

        int[] color = effect.getColor().rgbaArray();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        buffer.vertex(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z()).color(color[0], color[1], color[2], color[3]).uv(minU, maxV).endVertex();
        buffer.vertex(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z()).color(color[0], color[1], color[2], color[3]).uv(maxU, maxV).endVertex();
        buffer.vertex(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z()).color(color[0], color[1], color[2], color[3]).uv(maxU, minV).endVertex();
        buffer.vertex(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z()).color(color[0], color[1], color[2], color[3]).uv(minU, minV).endVertex();
        poseStack.popPose();
    }
}