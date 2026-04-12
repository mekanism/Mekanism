package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.lib.effect.CustomEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    public static void render(CustomEffect effect, CameraRenderState camera, MultiBufferSource renderer, PoseStack poseStack, int renderTick, float partialTick) {
        RenderType renderType = MekanismRenderType.SPS.apply(effect.getTexture());
        VertexConsumer buffer = renderer.getBuffer(renderType);
        int gridSize = effect.getTextureGridSize();

        int tick = renderTick % (gridSize * gridSize);
        int xIndex = tick % gridSize;
        int yIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;
        Vector3f[] vertexPos = {new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vec3 pos = effect.getPos(partialTick);
        for (Vector3f vector3f : vertexPos) {
            vector3f.rotate(camera.orientation);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }

        int argb = effect.getColor().argb();
        float minU = xIndex * spriteSize;
        float maxU = minU + spriteSize;
        float minV = yIndex * spriteSize;
        float maxV = minV + spriteSize;

        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z())
              .setUv(minU, maxV)
              .setColor(argb);
        buffer.addVertex(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z())
              .setUv(maxU, maxV)
              .setColor(argb);
        buffer.addVertex(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z())
              .setUv(maxU, minV)
              .setColor(argb);
        buffer.addVertex(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z())
              .setUv(minU, minV)
              .setColor(argb);
    }
}