package mekanism.client.render.bolt;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import mekanism.client.render.bolt.BoltRenderer.BoltData;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

public class BoltEffect {

    public static final BoltEffect ELECTRICITY = electricity(0.1F);

    private final Random random = new Random();

    /** How large the individual bolts should render. */
    private float size = 0.1F;

    /** How much variance is allowed in segment lengths (parallel to straight line). */
    private float parallelNoise = 0.1F;
    /** How much variance is allowed perpendicular to the straight line vector. Scaled by distance and spread function. */
    private float spreadFactor = 0.1F;

    /** The chance of creating an additional branch after a certain segment. */
    private float branchInitiationFactor;
    /** The chance of a branch continuing (post-initiation). */
    private float branchContinuationFactor;

    private float red = 0.45F, green = 0.45F, blue = 0.5F, alpha = 0.8F;

    private final SpreadFunction spreadFunction;
    private final RandomFunction randomFunction;
    private SegmentSpreader segmentSpreader = SegmentSpreader.NO_MEMORY;

    public static BoltEffect basic() {
        return new BoltEffect();
    }

    public static BoltEffect electricity(float size) {
        return new BoltEffect().withColor(0.54F, 0.91F, 1F, 0.8F).withNoise(0.2F, 0.3F).withBranching(0.1F, 0.6F)
              .withSpreader(SegmentSpreader.memory(0.85F)).withSize(size);
    }

    protected BoltEffect() {
        this(SpreadFunction.SINE, RandomFunction.GAUSSIAN);
    }

    protected BoltEffect(SpreadFunction spreadFunction, RandomFunction randomFunction) {
        this.spreadFunction = spreadFunction;
        this.randomFunction = randomFunction;
    }

    public BoltEffect withColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public BoltEffect withSize(float size) {
        this.size = size;
        return this;
    }

    public BoltEffect withNoise(float parallelNoise, float spreadFactor) {
        this.parallelNoise = parallelNoise;
        this.spreadFactor = spreadFactor;
        return this;
    }

    public BoltEffect withBranching(float branchInitiationFactor, float branchContinuationFactor) {
        this.branchInitiationFactor = branchInitiationFactor;
        this.branchContinuationFactor = branchContinuationFactor;
        return this;
    }

    public BoltEffect withSpreader(SegmentSpreader segmentSpreader) {
        this.segmentSpreader = segmentSpreader;
        return this;
    }

    public List<BoltQuads> generate(BoltData boltData) {
        List<BoltQuads> quads = new ArrayList<>();
        Vec3d diff = boltData.getEnd().subtract(boltData.getStart());
        float totalDistance = (float) diff.length();
        for (int i = 0; i < boltData.getCount(); i++) {
            Queue<BoltInstructions> drawQueue = new LinkedList<>();
            drawQueue.add(new BoltInstructions(boltData.getStart(), 0, new Vec3d(0, 0, 0), null, false));
            while (!drawQueue.isEmpty()) {
                BoltInstructions data = drawQueue.poll();
                Vec3d perpendicularDist = data.perpendicularDist;
                float progress = data.progress + (1F / boltData.getSegments()) * (1 - parallelNoise + random.nextFloat() * parallelNoise * 2);
                Vec3d segmentEnd = null;
                if (progress >= 1) {
                    segmentEnd = boltData.getEnd();
                } else {
                    float segmentDiffScale = spreadFunction.getMaxSpread(progress);
                    float maxDiff = spreadFactor * segmentDiffScale * totalDistance * randomFunction.getRandom(random);
                    Vec3d randVec = findRandomOrthogonalVector(diff, random);
                    perpendicularDist = segmentSpreader.getSegmentAdd(perpendicularDist, randVec, maxDiff, segmentDiffScale, progress);
                    // new vector is original + current progress through segments + perpendicular change
                    segmentEnd = boltData.getStart().add(diff.scale(progress)).add(perpendicularDist);
                }
                float boltSize = size * (0.5F + (1 - progress) * 0.5F);
                Pair<BoltQuads, QuadCache> quadData = createQuads(data.cache, data.start, segmentEnd, Color.rgba(red, green, blue, alpha), boltSize);
                quads.add(quadData.getLeft());

                if (segmentEnd == boltData.getEnd())
                // break if we've reached the defined end point
                {
                    break;
                }
                if (!data.isBranch) {
                    // continue the bolt if this is the primary (non-branch) segment
                    drawQueue.add(new BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.getRight(), false));
                } else if (random.nextFloat() < branchContinuationFactor) {
                    // branch continuation
                    drawQueue.add(new BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.getRight(), true));
                }
                while (random.nextFloat() < branchInitiationFactor * (1 - progress))
                // branch initiation (probability decreases as progress increases)
                {
                    drawQueue.add(new BoltInstructions(segmentEnd, progress, perpendicularDist, quadData.getRight(), true));
                }
            }
        }
        return quads;
    }

    private static Vec3d findRandomOrthogonalVector(Vec3d vec, Random rand) {
        Vec3d newVec = new Vec3d(-0.5 + rand.nextDouble(), -0.5 + rand.nextDouble(), -0.5 + rand.nextDouble());
        return vec.crossProduct(newVec).normalize();
    }

    private Pair<BoltQuads, QuadCache> createQuads(QuadCache cache, Vec3d startPos, Vec3d end, Color color, float size) {
        Vec3d diff = end.subtract(startPos);
        Vec3d rightAdd = diff.crossProduct(new Vec3d(0.5, 0.5, 0.5)).normalize().scale(size);
        Vec3d backAdd = diff.crossProduct(rightAdd).normalize().scale(size), rightAddSplit = rightAdd.scale(0.5F);

        Vec3d start = cache != null ? cache.prevEnd : startPos;
        Vec3d startRight = cache != null ? cache.prevEndRight : start.add(rightAdd);
        Vec3d startBack = cache != null ? cache.prevEndBack : start.add(rightAddSplit).add(backAdd);
        Vec3d endRight = end.add(rightAdd), endBack = end.add(rightAddSplit).add(backAdd);

        BoltQuads quads = new BoltQuads(color);
        quads.addQuad(start, end, endRight, startRight);
        quads.addQuad(startRight, endRight, end, start);

        quads.addQuad(startRight, endRight, endBack, startBack);
        quads.addQuad(startBack, endBack, endRight, startRight);

        return Pair.of(quads, new QuadCache(end, endRight, endBack));
    }

    private static class QuadCache {

        private final Vec3d prevEnd, prevEndRight, prevEndBack;

        private QuadCache(Vec3d prevEnd, Vec3d prevEndRight, Vec3d prevEndBack) {
            this.prevEnd = prevEnd;
            this.prevEndRight = prevEndRight;
            this.prevEndBack = prevEndBack;
        }
    }

    protected static class BoltInstructions {

        private final Vec3d start;
        private final Vec3d perpendicularDist;
        private final QuadCache cache;
        private final float progress;
        private final boolean isBranch;

        private BoltInstructions(Vec3d start, float progress, Vec3d perpendicularDist, QuadCache cache, boolean isBranch) {
            this.start = start;
            this.perpendicularDist = perpendicularDist;
            this.progress = progress;
            this.cache = cache;
            this.isBranch = isBranch;
        }
    }

    protected static class BoltQuads {

        private final Color color;
        private final List<Vec3d> vecs;

        protected BoltQuads(Color color) {
            this.color = color;
            vecs = new ArrayList<>();
        }

        protected void addQuad(Vec3d... quadVecs) {
            vecs.addAll(Arrays.asList(quadVecs));
        }

        protected void render(Matrix4f matrix, IVertexBuilder buffer, float alpha) {
            vecs.forEach(v -> buffer.pos(matrix, (float) v.x, (float) v.y, (float) v.z)
                  .color(color.r, color.g, color.b, (int) (color.a * alpha))
                  .endVertex());
        }
    }

    public interface SpreadFunction {

        /** A steady linear increase in perpendicular noise. */
        SpreadFunction LINEAR_ASCENT = (progress) -> progress;
        /** A steady linear increase in perpendicular noise, followed by a steady decrease after the halfway point. */
        SpreadFunction LINEAR_ASCENT_DESCENT = (progress) -> (progress - Math.max(0, 2 * progress - 1)) / 0.5F;
        /** Represents a unit sine wave from 0 to PI, scaled by progress. */
        SpreadFunction SINE = (progress) -> (float) Math.sin(Math.PI * progress);

        float getMaxSpread(float progress);
    }

    public interface RandomFunction {

        RandomFunction UNIFORM = Random::nextFloat;
        RandomFunction GAUSSIAN = rand -> (float) rand.nextGaussian();

        float getRandom(Random rand);
    }

    public interface SegmentSpreader {

        /** Don't remember where the last segment left off, just randomly move from the straight-line vector. */
        SegmentSpreader NO_MEMORY = (perpendicularDist, randVec, maxDiff, scale, progress) -> randVec.scale(maxDiff);

        /** Move from where the previous segment ended by a certain memory factor. Higher memory will restrict perpendicular movement. */
        static SegmentSpreader memory(float memoryFactor) {
            return (perpendicularDist, randVec, maxDiff, spreadScale, progress) -> {
                float nextDiff = maxDiff * (1 - memoryFactor);
                Vec3d cur = randVec.scale(nextDiff);
                if (progress > 0.5F) {
                    // begin to come back to the center after we pass halfway mark
                    cur = cur.add(perpendicularDist.scale(-1 * (1 - spreadScale)));
                }
                return perpendicularDist.add(cur);
            };
        }

        Vec3d getSegmentAdd(Vec3d perpendicularDist, Vec3d randVec, float maxDiff, float scale, float progress);
    }
}
