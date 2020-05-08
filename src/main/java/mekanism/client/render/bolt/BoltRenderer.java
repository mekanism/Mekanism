package mekanism.client.render.bolt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.client.render.bolt.BoltEffect.BoltQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.Vec3d;

public class BoltRenderer {
    /** Amount of times per tick we refresh. 3 implies 60 Hz. */
    private static final float REFRESH_TIME = 3F;
    /** We will keep track of an owner's render data for 100 ticks after there are no bolts remaining. */
    private static final double MAX_OWNER_TRACK_TIME = 100;
    private float refreshTimestamp;

    private final Random random = new Random();
    private final Minecraft minecraft = Minecraft.getInstance();

    private final int boltLifespan;

    private final BoltEffect boltEffect;
    private final SpawnFunction spawnFunction;
    private final FadeFunction fadeFunction;

    private boolean repeat;

    private Map<Object, BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap<>();

    public static BoltRenderer create(BoltEffect boltEffect) {
        return create(boltEffect, 60);
    }

    public static BoltRenderer create(BoltEffect boltEffect, int rate) {
        return create(boltEffect, rate / 2, SpawnFunction.delay(rate));
    }

    public static BoltRenderer create(BoltEffect boltEffect, int lifespan, SpawnFunction spawnFunction) {
        return create(boltEffect, lifespan, spawnFunction, FadeFunction.fade(0.5F));
    }

    public static BoltRenderer create(BoltEffect boltEffect, int boltLifespan, SpawnFunction spawnFunction, FadeFunction fadeFunction) {
        return new BoltRenderer(boltEffect, boltLifespan, spawnFunction, fadeFunction);
    }

    public BoltRenderer repeat() {
        repeat = true;
        return this;
    }

    protected BoltRenderer(BoltEffect boltEffect, int boltLifespan, SpawnFunction spawnFunction, FadeFunction fadeFunction) {
        this.boltEffect = boltEffect;
        this.boltLifespan = boltLifespan;
        this.spawnFunction = spawnFunction;
        this.fadeFunction = fadeFunction;
    }

    public void render(float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        double time = minecraft.world.getGameTime() + partialTicks;

        if (partialTicks < refreshTimestamp)
            partialTicks += 1;
        boolean refresh = partialTicks - refreshTimestamp >= (1 / REFRESH_TIME);
        if (refresh) {
            refreshTimestamp = partialTicks % 1;
        }

        for (Iterator<Map.Entry<Object, BoltOwnerData>> iter = boltOwners.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Object, BoltOwnerData> entry = iter.next();
            BoltOwnerData data = entry.getValue();
            // tick our bolts based on the refresh rate, removing if they're now finished
            if (refresh) {
                data.bolts.removeIf(bolt -> bolt.tick());
            }
            if (repeat && data.bolts.isEmpty() && data.lastBolt != null) {
                data.bolts.add(new BoltInstance(boltLifespan, boltEffect.generate(data.lastBolt)));
            }
            data.bolts.forEach(bolt -> bolt.render(matrix, buffer));

            if (data.bolts.isEmpty() && time - data.lastUpdateTimestamp >= MAX_OWNER_TRACK_TIME) {
                iter.remove();
            }
        }
    }

    public void update(Object owner, BoltData newBoltData, float partialTicks) {
        if (minecraft.world == null)
            return;

        BoltOwnerData data = boltOwners.computeIfAbsent(owner, o -> new BoltOwnerData());
        double time = minecraft.world.getGameTime() + partialTicks;
        if (!repeat && time - data.lastBoltTimestamp >= spawnFunction.getSpawnDelay(random)) {
            data.lastBoltTimestamp = time;
            data.bolts.add(new BoltInstance(boltLifespan, boltEffect.generate(newBoltData)));
        }
        data.lastUpdateTimestamp = time;
        data.lastBolt = newBoltData;
    }

    public static class BoltOwnerData {

        private Set<BoltInstance> bolts = new ObjectOpenHashSet<>();
        private BoltData lastBolt;
        private double lastBoltTimestamp;
        private double lastUpdateTimestamp;
    }

    public static class BoltData {

        private Vec3d start;
        private Vec3d end;
        private int count;
        private int segments;

        public BoltData(Vec3d start, Vec3d end, int count, int segments) {
            this.start = start;
            this.end = end;
            this.count = count;
            this.segments = segments;
        }

        public Vec3d getStart() {
            return start;
        }

        public Vec3d getEnd() {
            return end;
        }

        public int getCount() {
            return count;
        }

        public int getSegments() {
            return segments;
        }
    }

    public class BoltInstance {

        private List<BoltQuads> renderQuads = new ArrayList<>();
        private final float lifespan;
        private int ticksExisted;

        public BoltInstance(float lifespan, List<BoltQuads> renderQuads) {
            this.lifespan = lifespan;
            this.renderQuads = renderQuads;
        }

        public void render(Matrix4f matrix, IVertexBuilder buffer) {
            float lifeScale = ticksExisted / lifespan;
            Pair<Integer, Integer> bounds = fadeFunction.getRenderBounds(renderQuads.size(), lifeScale);
            for (int i = bounds.getLeft(); i < bounds.getRight(); i++) {
                renderQuads.get(i).render(matrix, buffer, 1);
            }
        }

        public boolean tick() {
            return ticksExisted++ >= lifespan;
        }
    }

    public interface SpawnFunction {
        /** Allow for bolts to be spawned each update call without any delay. */
        public static SpawnFunction NO_DELAY = (rand) -> Pair.of(0F, 0F);

        /** Spawn bolts with a specified constant delay. */
        public static SpawnFunction delay(float delay) {
            return (rand) -> Pair.of(delay, delay);
        }

        /**
         * Spawns bolts with a specified delay and specified noise value, which will be randomly applied at
         * either end of the delay bounds.
         */
        public static SpawnFunction noise(float delay, float noise) {
            return (rand) -> Pair.of(delay - noise, delay + noise);
        }

        Pair<Float, Float> getSpawnDelayBounds(Random rand);

        default float getSpawnDelay(Random rand) {
            Pair<Float, Float> bounds = getSpawnDelayBounds(rand);
            return bounds.getLeft() + (bounds.getRight() - bounds.getLeft()) * rand.nextFloat();
        }
    }

    public interface FadeFunction {
        /** No fade; render the bolts entirely throughout their lifespan. */
        public static FadeFunction NONE = (totalBolts, lifeScale) -> Pair.of(0, totalBolts);

        /** Remder bolts with a segment-by-segment 'fade' in and out, with a specified fade duration (applied to start and finish). */
        public static FadeFunction fade(float fade) {
            return (totalBolts, lifeScale) -> {
                int start = lifeScale > (1 - fade) ? (int) (totalBolts * (lifeScale - (1 - fade)) / fade) : 0;
                int end = lifeScale < fade ? (int) (totalBolts * (lifeScale / fade)) : totalBolts;
                return Pair.of(start, end);
            };
        }

        Pair<Integer, Integer> getRenderBounds(int totalBolts, float lifeScale);
    }
}
