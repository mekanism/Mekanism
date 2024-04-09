package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.common.util.EnumUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class Outlines {

    public static List<Line> extract(BakedModel model, @Nullable BlockState state, RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        List<Line> lines = new ArrayList<>();
        VertexExtractor consumer = new VertexExtractor(lines);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            for (BakedQuad quad : model.getQuads(state, direction, rand, modelData, renderType)) {
                consumer.unpack(quad);
            }
        }

        for (BakedQuad quad : model.getQuads(state, null, rand, modelData, renderType)) {
            consumer.unpack(quad);
        }
        return lines;
    }

    //modified version of VertexConsumer
    @MethodsReturnNonnullByDefault
    private static class VertexExtractor {

        final List<Line> lines;
        final Vector3f[] vertices = new Vector3f[4];
        int vertexIndex = 0;

        private VertexExtractor(List<Line> lines) {
            this.lines = lines;
        }

        public void vertex(float pX, float pY, float pZ) {
            vertices[vertexIndex++] = new Vector3f(pX, pY, pZ);
            if (vertexIndex == 4) {
                vertexIndex = 0;
                addLine(vertices[0], vertices[1]);
                addLine(vertices[1], vertices[2]);
                addLine(vertices[2], vertices[3]);
                addLine(vertices[3], vertices[0]);
                Arrays.fill(vertices, null);
            }
        }

        private void addLine(Vector3f v1, Vector3f v2) {
            Line line = new Line(v1, v2);
            if (!lines.contains(line)) {
                lines.add(line);
            }
        }

        //Based on how QuadTransformers#applying extracts the vertex positions
        public void unpack(BakedQuad pQuad) {
            int[] quadVertices = pQuad.getVertices();
            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                float x = Float.intBitsToFloat(quadVertices[offset]);
                float y = Float.intBitsToFloat(quadVertices[offset + 1]);
                float z = Float.intBitsToFloat(quadVertices[offset + 2]);
                this.vertex(x, y, z);
            }
        }
    }

    public record Line(float x1, float y1, float z1, float x2, float y2, float z2, float nX, float nY, float nZ) {

        public Line(Vector3f v1, Vector3f v2, Vector3f normal) {
            this(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, normal.x, normal.y, normal.z);
        }

        public Line(Vector3f v1, Vector3f v2) {
            this(v1, v2, v2.sub(v1, new Vector3f()).normalize());
        }

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || obj.getClass() != Line.class) {
                return false;
            }
            Line other = (Line) obj;
            return (Mth.equal(x1, other.x1) && Mth.equal(y1, other.y1) && Mth.equal(z1, other.z1) && Mth.equal(x2, other.x2) && Mth.equal(y2, other.y2) && Mth.equal(z2, other.z2)) ||
                   (Mth.equal(x1, other.x2) && Mth.equal(y1, other.y2) && Mth.equal(z1, other.z2) && Mth.equal(x2, other.x1) && Mth.equal(y2, other.y1) && Mth.equal(z2, other.z1));
        }
    }
}
