package mekanism.client.render;

import java.util.Random;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.Vec3d;

public class BoltRenderer {

    // max distance perpendicular to straight line
    private static float maxDiffScale = 0.3F;
    // max distance perpendicular to straight line in single step

    public static void render(Vec3d start, Vec3d end, int count, int segments, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        Random random = new Random();
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        Vec3d diff = end.subtract(start);
        float distance = (float) diff.length();
        for (int i = 0; i < count; i++) {
            Vec3d segmentStart = start;
            for (int segment = 0; segment < segments; segment++) {
                // last segment
                Vec3d segmentEnd = null;
                if (segment == segments - 1) {
                    segmentEnd = end;
                } else {
                    // represents how far we can be (perpendicular from the line) at this end segment
                    float segmentDiffScale = (segment + 1 - Math.max(0, 2 * segment + 1 - (segments - 1))) / (segments - 1 / 2F);
                    float maxDiff = maxDiffScale * segmentDiffScale * distance * random.nextFloat();
                    Vec3d cur = findRandomOrthogonalVector(diff, random).scale(maxDiff);
                    // new vector is original + current progress through segments + perpendicular change
                    segmentEnd = start.add(diff.scale((segment + 1) / (float) segments)).add(cur);
                }
                //System.out.println(segmentStart + " " + segmentEnd);
                renderBolt(matrix, buffer, segmentStart, segmentEnd, 0.45F, 0.45F, 0.5F);
                segmentStart = segmentEnd;
            }
        }
    }

    private static Vec3d findRandomOrthogonalVector(Vec3d vec, Random rand) {
        Vec3d newVec = new Vec3d(-0.5 + rand.nextDouble(), -0.5 + rand.nextDouble(), -0.5 + rand.nextDouble());
        return vec.crossProduct(newVec).normalize();
    }

    private static void renderBolt(Matrix4f matrix, IVertexBuilder buffer, Vec3d start, Vec3d end, float r, float g, float b) {
        // start bottom
        buffer.pos(matrix, (float) start.x, (float) start.y, (float) start.z)
            .color(r, g, b, 0.3F).endVertex();
        buffer.pos(matrix, (float) start.x+0.1F, (float) start.y, (float) start.z)
            .color(r, g, b, 0.3F).endVertex();
        buffer.pos(matrix, (float) start.x+0.05F, (float) start.y, (float) start.z+0.1F)
            .color(r, g, b, 0.3F).endVertex();

        buffer.pos(matrix, (float) end.x, (float) end.y, (float) end.z)
            .color(r, g, b, 0.3F).endVertex();
        buffer.pos(matrix, (float) end.x+0.1F, (float) end.y, (float) end.z)
            .color(r, g, b, 0.3F).endVertex();
        buffer.pos(matrix, (float) end.x+0.05F, (float) end.y, (float) end.z+0.1F)
            .color(r, g, b, 0.3F).endVertex();
    }
}
