package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;

public class BoltRenderer {

    /** Amount of times per tick we refresh. 3 implies 60 Hz. */
    private static final float REFRESH_TIME = 3F;
    /** We will keep track of an owner's render data for 100 ticks after there are no bolts remaining. */
    private static final double MAX_OWNER_TRACK_TIME = 100;

    private Timestamp refreshTimestamp = new Timestamp();

    private final Random random = new Random();
    private final Minecraft minecraft = Minecraft.getInstance();

    private final Map<Object, BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap<>();

    public void render(float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn) {
        IVertexBuilder buffer = bufferIn.getBuffer(MekanismRenderType.MEK_LIGHTNING);
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Timestamp timestamp = new Timestamp(minecraft.world.getGameTime(), partialTicks);
        boolean refresh = timestamp.isPassed(refreshTimestamp, (1 / REFRESH_TIME));
        if (refresh) {
            refreshTimestamp = timestamp;
        }
        synchronized (boltOwners) {
            for (Iterator<Map.Entry<Object, BoltOwnerData>> iter = boltOwners.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<Object, BoltOwnerData> entry = iter.next();
                BoltOwnerData data = entry.getValue();
                // tick our bolts based on the refresh rate, removing if they're now finished
                if (refresh) {
                    data.bolts.removeIf(bolt -> bolt.tick(timestamp));
                }
                if (data.bolts.isEmpty() && data.lastBolt != null && data.lastBolt.getSpawnFunction().isConsecutive()) {
                    data.addBolt(new BoltInstance(data.lastBolt, timestamp), timestamp);
                }
                data.bolts.forEach(bolt -> bolt.render(matrix, buffer, timestamp));

                if (data.bolts.isEmpty() && timestamp.isPassed(data.lastUpdateTimestamp, MAX_OWNER_TRACK_TIME)) {
                    iter.remove();
                }
            }
        }
    }

    public void update(Object owner, BoltEffect newBoltData, float partialTicks) {
        if (minecraft.world == null) {
            return;
        }
        synchronized (boltOwners) {
            BoltOwnerData data = boltOwners.computeIfAbsent(owner, o -> new BoltOwnerData());
            data.lastBolt = newBoltData;
            Timestamp timestamp = new Timestamp(minecraft.world.getGameTime(), partialTicks);
            if ((!data.lastBolt.getSpawnFunction().isConsecutive() || data.bolts.isEmpty()) && timestamp.isPassed(data.lastBoltTimestamp, data.lastBoltDelay)) {
                data.addBolt(new BoltInstance(newBoltData, timestamp), timestamp);
            }
            data.lastUpdateTimestamp = timestamp;
        }
    }

    public class BoltOwnerData {

        private final Set<BoltInstance> bolts = new ObjectOpenHashSet<>();
        private BoltEffect lastBolt;
        private Timestamp lastBoltTimestamp = new Timestamp();
        private Timestamp lastUpdateTimestamp = new Timestamp();
        private double lastBoltDelay;

        private void addBolt(BoltInstance instance, Timestamp timestamp) {
            bolts.add(instance);
            lastBoltDelay = instance.bolt.getSpawnFunction().getSpawnDelay(random);
            lastBoltTimestamp = timestamp;
        }
    }

    public static class BoltInstance {

        private final BoltEffect bolt;
        private final List<BoltQuads> renderQuads;
        private final Timestamp createdTimestamp;

        public BoltInstance(BoltEffect bolt, Timestamp timestamp) {
            this.bolt = bolt;
            this.renderQuads = bolt.generate();
            this.createdTimestamp = timestamp;
        }

        public void render(Matrix4f matrix, IVertexBuilder buffer, Timestamp timestamp) {
            float lifeScale = timestamp.subtract(createdTimestamp).value() / bolt.getLifespan();
            Pair<Integer, Integer> bounds = bolt.getFadeFunction().getRenderBounds(renderQuads.size(), lifeScale);
            for (int i = bounds.getLeft(); i < bounds.getRight(); i++) {
                renderQuads.get(i).getVecs().forEach(v -> buffer.pos(matrix, (float) v.x, (float) v.y, (float) v.z)
                      .color(bolt.getColor().r(), bolt.getColor().g(), bolt.getColor().b(), bolt.getColor().a())
                      .endVertex());
            }
        }

        public boolean tick(Timestamp timestamp) {
            return timestamp.isPassed(createdTimestamp, bolt.getLifespan());
        }
    }

    public static class Timestamp {

        private final long ticks;
        private final float partial;

        public Timestamp() {
            this(0, 0);
        }

        public Timestamp(long ticks, float partial) {
            this.ticks = ticks;
            this.partial = partial;
        }

        public Timestamp subtract(Timestamp other) {
            long newTicks = ticks - other.ticks;
            float newPartial = partial - other.partial;
            if (newPartial < 0) {
                newPartial += 1;
                newTicks -= 1;
            }
            return new Timestamp(newTicks, newPartial);
        }

        public float value() {
            return ticks + partial;
        }

        public boolean isPassed(Timestamp prev, double duration) {
            long ticksPassed = ticks - prev.ticks;
            if (ticksPassed > duration) {
                return true;
            }
            duration -= ticksPassed;
            if (duration >= 1) {
                return false;
            }
            return (partial - prev.partial) >= duration;
        }
    }
}
