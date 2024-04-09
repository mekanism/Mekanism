package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.common.util.EnumUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.lwjgl.system.MemoryStack;

public class Outlines {

    public static List<Line> extract(BakedModel model, BlockState state, RandomSource rand, ModelData modelData) {
        List<Line> lines = new ArrayList<>();
        VertexExtractor consumer = new VertexExtractor(lines);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            //TODO: Eventually we may want to add support for Model data and maybe render type
            for (BakedQuad quad : model.getQuads(state, direction, rand, modelData, null)) {
                consumer.unpack(quad);
            }
        }

        for (BakedQuad quad : model.getQuads(state, null, rand, modelData, null)) {
            consumer.unpack(quad);
        }
        return lines;
    }

    //modified version of VertexConsumer
    @MethodsReturnNonnullByDefault
    private static class VertexExtractor {

        final List<Line> lines;
        final Vec3[] vertices = new Vec3[4];
        int vertexIndex = 0;

        private VertexExtractor(List<Line> lines) {
            this.lines = lines;
        }

        public void vertex(double pX, double pY, double pZ) {
            vertices[vertexIndex++] = new Vec3(pX, pY, pZ);
            if (vertexIndex == 4) {
                vertexIndex = 0;
                addLine(vertices[0].x, vertices[0].y, vertices[0].z, vertices[1].x, vertices[1].y, vertices[1].z);
                addLine(vertices[1].x, vertices[1].y, vertices[1].z, vertices[2].x, vertices[2].y, vertices[2].z);
                addLine(vertices[2].x, vertices[2].y, vertices[2].z, vertices[3].x, vertices[3].y, vertices[3].z);
                addLine(vertices[3].x, vertices[3].y, vertices[3].z, vertices[0].x, vertices[0].y, vertices[0].z);
                Arrays.fill(vertices, null);
            }
        }

        //slimmed down version of putBulkData
        public void unpack(BakedQuad pQuad) {
            int[] quadVertices = pQuad.getVertices();
            int elementCount = quadVertices.length / 8;

            try (MemoryStack memorystack = MemoryStack.stackPush()) {
                ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
                IntBuffer intbuffer = bytebuffer.asIntBuffer();

                for (int k = 0; k < elementCount; ++k) {
                    intbuffer.clear();
                    intbuffer.put(quadVertices, k * 8, 8);
                    float f = bytebuffer.getFloat(0);
                    float f1 = bytebuffer.getFloat(4);
                    float f2 = bytebuffer.getFloat(8);
                    this.vertex(f, f1, f2);
                }
            }
        }

        private void addLine(double x1, double y1, double z1, double x2, double y2, double z2) {
            double nX = (x2 - x1);
            double nY = (y2 - y1);
            double nZ = (z2 - z1);
            double nLen = Math.sqrt(nX * nX + nY * nY + nZ * nZ);

            nX = nX / nLen;
            nY = nY / nLen;
            nZ = nZ / nLen;

            lines.add(new Line((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, (float) nX, (float) nY, (float) nZ));
        }

    }

    public record Line(float x1, float y1, float z1, float x2, float y2, float z2, float nX, float nY, float nZ) {}
}
