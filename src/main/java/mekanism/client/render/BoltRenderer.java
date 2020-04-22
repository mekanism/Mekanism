package mekanism.client.render;

import java.util.Random;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.Vec3d;

public class BoltRenderer {

    private Random random = new Random();

    /** How large the individual bolts should render. */
    private float size = 0.05F;
    /** How much variance is allowed in segment lengths (parallel to straight line). */
    private float parallelNoise;
    /** How much variance is allowed perpendicular to the straight line vector. Scaled by distance and spread function. */
    private float spreadFactor;

    private float red = 0.45F, green = 0.45F, blue = 0.5F, alpha = 0.8F;

    private SpreadFunction spreadFunction;
    private RandomFunction randomFunction;

    // These caches where the last segment renders left off, allowing us to 'seal' each bolt segment to the previous.
    private Vec3d prevEnd, prevEndRight, prevEndBack;

    public BoltRenderer() {
        this(SpreadFunction.SINE, RandomFunction.GAUSSIAN);
    }

    public BoltRenderer(SpreadFunction spreadFunction, RandomFunction randomFunction) {
        this(spreadFunction, randomFunction, 0.1F, 0.1F);
    }

    public BoltRenderer(SpreadFunction spreadFunction, RandomFunction randomFunction, float parallelNoise, float spreadFactor) {
        this.spreadFunction = spreadFunction;
        this.randomFunction = randomFunction;
        this.parallelNoise = parallelNoise;
        this.spreadFactor = spreadFactor;
    }

    public BoltRenderer withColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public BoltRenderer withSize(float size) {
        this.size = size;
        return this;
    }

    public void render(Vec3d start, Vec3d end, int count, int segments, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();

        Vec3d diff = end.subtract(start);
        float totalDistance = (float) diff.length();
        for (int i = 0; i < count; i++) {
            Vec3d segmentStart = start;
            float progress = 0;
            while (segmentStart != end) {
                progress = progress + (1F / segments) * (1 - parallelNoise + random.nextFloat() * parallelNoise * 2);
                Vec3d segmentEnd = null;
                if (progress >= 1) {
                    segmentEnd = end;
                } else {
                    float segmentDiffScale = spreadFunction.getMaxSpread(progress);
                    float maxDiff = spreadFactor * segmentDiffScale * totalDistance * randomFunction.getRandom(random);
                    Vec3d cur = findRandomOrthogonalVector(diff, random).scale(maxDiff);
                    // new vector is original + current progress through segments + perpendicular change
                    segmentEnd = start.add(diff.scale(progress)).add(cur);
                }
                renderBolt(matrix, buffer, segmentStart, segmentEnd);
                segmentStart = segmentEnd;
            }
            prevEnd = prevEndRight = prevEndBack = null;
        }
    }

    private static Vec3d findRandomOrthogonalVector(Vec3d vec, Random rand) {
        Vec3d newVec = new Vec3d(-0.5 + rand.nextDouble(), -0.5 + rand.nextDouble(), -0.5 + rand.nextDouble());
        return vec.crossProduct(newVec).normalize();
    }

    private void renderBolt(Matrix4f matrix, IVertexBuilder buffer, Vec3d startPos, Vec3d end) {
        Vec3d diff = end.subtract(startPos);
        Vec3d rightAdd = diff.crossProduct(new Vec3d(0.5, 0.5, 0.5)).normalize().scale(size);
        Vec3d backAdd = diff.crossProduct(rightAdd).normalize().scale(size), rightAddSplit = rightAdd.scale(0.5F);

        Vec3d start = prevEnd != null ? prevEnd : startPos;
        Vec3d startRight = prevEndRight != null ? prevEndRight : start.add(rightAdd);
        Vec3d startBack = prevEndBack != null ? prevEndBack : start.add(rightAddSplit).add(backAdd);
        Vec3d endRight = end.add(rightAdd), endBack = end.add(rightAddSplit).add(backAdd);

        prevEnd = end; prevEndRight = endRight; prevEndBack = endBack;

        draw(matrix, buffer, start, end, endRight, startRight);
        draw(matrix, buffer, startRight, endRight, end, start);

        draw(matrix, buffer, startRight, endRight, endBack, startBack);
        draw(matrix, buffer, startBack, endBack, endRight, startRight);
    }

    private void draw(Matrix4f matrix, IVertexBuilder buffer, Vec3d... vertices) {
        for (Vec3d vec : vertices) {
            buffer.pos(matrix, (float) vec.x, (float) vec.y, (float) vec.z).color(red, green, blue, alpha).endVertex();
        }
    }

    public interface SpreadFunction {
        /** A steady linear increase in perpendicular noise. */
        public static final SpreadFunction LINEAR_ASCENT = (progress) -> progress;
        /** A steady linear increase in perpendicular noise, followed by a steady decrease after the halfway point. */
        public static final SpreadFunction LINEAR_ASCENT_DESCENT = (progress) -> (progress - Math.max(0, 2 * progress - 1)) / (0.5F);
        /** Represents a unit sine wave from 0 to PI, scaled by progress. */
        public static final SpreadFunction SINE = (progress) -> (float) Math.sin(Math.PI * progress);

        float getMaxSpread(float progress);
    }

    public interface RandomFunction {

        public static final RandomFunction UNIFORM = (rand) -> rand.nextFloat();
        public static final RandomFunction GAUSSIAN = (rand) -> (float) rand.nextGaussian();

        float getRandom(Random rand);
    }
}
