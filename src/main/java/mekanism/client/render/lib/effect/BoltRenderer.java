package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltQuads;
import mekanism.common.lib.effect.BoltEffect.FadeFunction.RenderBounds;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BoltRenderer {

    /** Amount of times per tick we refresh. 3 implies 60 Hz. */
    private static final float REFRESH_TIME = 3F;
    /** We will keep track of an owner's render data for 5 seconds after there are no bolts remaining. */
    private static final double MAX_OWNER_TRACK_TIME = 5 * SharedConstants.TICKS_PER_SECOND;

    private Timestamp refreshTimestamp = new Timestamp();

    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft = Minecraft.getInstance();

    private final Map<Object, BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap<>();

    public boolean hasBoltsToRender() {
        synchronized (boltOwners) {
            return boltOwners.values().stream().anyMatch(data -> !data.bolts.isEmpty());
        }
    }

    public void render(float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn) {
        render(partialTicks, matrixStack, bufferIn, null);
    }

    public void render(float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, @Nullable Vec3 cameraPos) {
        VertexConsumer buffer = bufferIn.getBuffer(MekanismRenderType.MEK_LIGHTNING);
        Matrix4f matrix = matrixStack.last().pose();
        Timestamp timestamp = new Timestamp(minecraft.level.getGameTime(), partialTicks);
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
                    tickAndRemove(data, timestamp);
                }
                if (data.bolts.isEmpty() && data.lastBolt != null && data.lastBolt.getSpawnFunction().isConsecutive()) {
                    data.addBolt(new BoltInstance(data.lastBolt, timestamp), timestamp, random);
                }
                for (BoltInstance bolt : data.bolts) {
                    bolt.render(matrix, buffer, timestamp, cameraPos);
                }

                if (data.bolts.isEmpty() && timestamp.isPassed(data.lastUpdateTimestamp, MAX_OWNER_TRACK_TIME)) {
                    iter.remove();
                }
            }
        }
    }

    private static void tickAndRemove(BoltOwnerData data, Timestamp timestamp) {
        Iterator<BoltInstance> iterator = data.bolts.iterator();
        //noinspection Java8CollectionRemoveIf: requires capture
        while (iterator.hasNext()) {
            if (iterator.next().tick(timestamp)) {
                iterator.remove();
            }
        }
    }

    public void update(Object owner, BoltEffect newBoltData, float partialTicks) {
        if (minecraft.level == null) {
            return;
        }
        synchronized (boltOwners) {
            BoltOwnerData data = boltOwners.computeIfAbsent(owner, o -> new BoltOwnerData());
            data.lastBolt = newBoltData;
            Timestamp timestamp = new Timestamp(minecraft.level.getGameTime(), partialTicks);
            if ((!data.lastBolt.getSpawnFunction().isConsecutive() || data.bolts.isEmpty()) && timestamp.isPassed(data.lastBoltTimestamp, data.lastBoltDelay)) {
                data.addBolt(new BoltInstance(newBoltData, timestamp), timestamp, random);
            }
            data.lastUpdateTimestamp = timestamp;
        }
    }

    public static class BoltOwnerData {

        private final Set<BoltInstance> bolts = new ObjectOpenHashSet<>();
        private BoltEffect lastBolt;
        private Timestamp lastBoltTimestamp = new Timestamp();
        private Timestamp lastUpdateTimestamp = new Timestamp();
        private double lastBoltDelay;

        private void addBolt(BoltInstance instance, Timestamp timestamp, RandomSource random) {
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

        public void render(Matrix4f matrix, VertexConsumer buffer, Timestamp timestamp, @Nullable Vec3 cameraPos) {
            float lifeScale = timestamp.subtract(createdTimestamp).value() / bolt.getLifespan();
            RenderBounds bounds = bolt.getFadeFunction().getRenderBounds(renderQuads.size(), lifeScale);
            for (int i = bounds.start(); i < bounds.end(); i++) {
                for (Vec3 v : renderQuads.get(i).getVecs()) {
                    Vec3 shiftedVertex = cameraPos == null ? v : v.subtract(cameraPos);
                    buffer.addVertex(matrix, (float) shiftedVertex.x, (float) shiftedVertex.y, (float) shiftedVertex.z)
                          .setColor(bolt.getColor().r(), bolt.getColor().g(), bolt.getColor().b(), bolt.getColor().a());
                }
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
