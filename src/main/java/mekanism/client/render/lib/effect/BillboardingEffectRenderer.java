package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.lib.effect.CustomEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class BillboardingEffectRenderer {

    private BillboardingEffectRenderer() {
    }

    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void render(CustomEffect effect, BlockPos renderPos, MatrixStack matrixStack, IRenderTypeBuffer renderer, long time, float partialTick) {
        matrixStack.pushPose();
        int gridSize = effect.getTextureGridSize();
        IVertexBuilder buffer = getRenderBuffer(renderer, effect.getTexture());
        Matrix4f matrix = matrixStack.last().pose();
        ActiveRenderInfo renderInfo = minecraft.gameRenderer.getMainCamera();
        int tick = (int) time % (gridSize * gridSize);
        int xIndex = tick % gridSize, yIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;
        Quaternion quaternion = renderInfo.rotation();
        Vector3f[] vertexPos = new Vector3f[]{new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                              new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vector3d pos = effect.getPos(partialTick).subtract(Vector3d.atLowerCornerOf(renderPos));
        for (int i = 0; i < 4; i++) {
            Vector3f vector3f = vertexPos[i];
            vector3f.transform(quaternion);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.x(), (float) pos.y(), (float) pos.z());
        }

        int[] color = effect.getColor().rgbaArray();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        buffer.vertex(matrix, vertexPos[0].x(), vertexPos[0].y(), vertexPos[0].z()).color(color[0], color[1], color[2], color[3]).uv(minU, maxV).endVertex();
        buffer.vertex(matrix, vertexPos[1].x(), vertexPos[1].y(), vertexPos[1].z()).color(color[0], color[1], color[2], color[3]).uv(maxU, maxV).endVertex();
        buffer.vertex(matrix, vertexPos[2].x(), vertexPos[2].y(), vertexPos[2].z()).color(color[0], color[1], color[2], color[3]).uv(maxU, minV).endVertex();
        buffer.vertex(matrix, vertexPos[3].x(), vertexPos[3].y(), vertexPos[3].z()).color(color[0], color[1], color[2], color[3]).uv(minU, minV).endVertex();
        matrixStack.popPose();
    }

    protected static IVertexBuilder getRenderBuffer(IRenderTypeBuffer renderer, ResourceLocation texture) {
        return renderer.getBuffer(MekanismRenderType.renderSPS(texture));
    }
}
