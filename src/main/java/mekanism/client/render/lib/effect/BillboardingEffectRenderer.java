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
        matrixStack.push();
        int gridSize = effect.getTextureGridSize();
        IVertexBuilder buffer = getRenderBuffer(renderer, effect.getTexture());
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        ActiveRenderInfo renderInfo = minecraft.gameRenderer.getActiveRenderInfo();
        int tick = (int) time % (gridSize * gridSize);
        int xIndex = tick % gridSize, yIndex = tick / gridSize;
        float spriteSize = 1F / gridSize;
        Quaternion quaternion = renderInfo.getRotation();
        new Vector3f(-1.0F, -1.0F, 0.0F).transform(quaternion);
        Vector3f[] vertexPos = new Vector3f[]{new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
                                              new Vector3f(1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, -1.0F, 0.0F)};
        Vector3d pos = effect.getPos(partialTick).subtract(Vector3d.copy(renderPos));
        for (int i = 0; i < 4; i++) {
            Vector3f vector3f = vertexPos[i];
            vector3f.transform(quaternion);
            vector3f.mul(effect.getScale());
            vector3f.add((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
        }

        int[] color = effect.getColor().rgbaArray();
        float minU = xIndex * spriteSize, maxU = minU + spriteSize;
        float minV = yIndex * spriteSize, maxV = minV + spriteSize;

        buffer.pos(matrix, vertexPos[0].getX(), vertexPos[0].getY(), vertexPos[0].getZ()).color(color[0], color[1], color[2], color[3]).tex(minU, maxV).endVertex();
        buffer.pos(matrix, vertexPos[1].getX(), vertexPos[1].getY(), vertexPos[1].getZ()).color(color[0], color[1], color[2], color[3]).tex(maxU, maxV).endVertex();
        buffer.pos(matrix, vertexPos[2].getX(), vertexPos[2].getY(), vertexPos[2].getZ()).color(color[0], color[1], color[2], color[3]).tex(maxU, minV).endVertex();
        buffer.pos(matrix, vertexPos[3].getX(), vertexPos[3].getY(), vertexPos[3].getZ()).color(color[0], color[1], color[2], color[3]).tex(minU, minV).endVertex();
        matrixStack.pop();
    }

    protected static IVertexBuilder getRenderBuffer(IRenderTypeBuffer renderer, ResourceLocation texture) {
        return renderer.getBuffer(MekanismRenderType.renderSPS(texture));
    }
}
