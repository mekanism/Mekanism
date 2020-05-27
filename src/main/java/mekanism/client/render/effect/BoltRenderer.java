package mekanism.client.render.effect;

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
import mekanism.common.particle.custom.BoltEffect;
import mekanism.common.particle.custom.BoltEffect.BoltQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;

public class BoltRenderer {

    /** Amount of times per tick we refresh. 3 implies 60 Hz. */
    private static final float REFRESH_TIME = 3F;
    /** We will keep track of an owner's render data for 100 ticks after there are no bolts remaining. */
    private static final double MAX_OWNER_TRACK_TIME = 100;
    private float refreshTimestamp;

    private final Random random = new Random();
    private final Minecraft minecraft = Minecraft.getInstance();

    private final Map<Object, BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap<>();

    public void render(float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        double time = minecraft.world.getGameTime() + partialTicks;

        if (partialTicks < refreshTimestamp) {
            partialTicks += 1;
        }
        boolean refresh = partialTicks - refreshTimestamp >= (1 / REFRESH_TIME);
        if (refresh) {
            refreshTimestamp = partialTicks % 1;
        }
        for (Iterator<Map.Entry<Object, BoltOwnerData>> iter = boltOwners.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Object, BoltOwnerData> entry = iter.next();
            BoltOwnerData data = entry.getValue();
            // tick our bolts based on the refresh rate, removing if they're now finished
            if (refresh) {
                data.bolts.removeIf(BoltInstance::tick);
            }
            if (data.repeat && data.bolts.isEmpty() && data.lastBolt != null) {
                data.addBolt(new BoltInstance(data.lastBolt), time);
            }
            data.bolts.forEach(bolt -> bolt.render(matrix, buffer));

            if (data.bolts.isEmpty() && time - data.lastUpdateTimestamp >= MAX_OWNER_TRACK_TIME) {
                iter.remove();
            }
        }
    }

    public void update(Object owner, BoltEffect newBoltData, float partialTicks) {
        if (minecraft.world == null) {
            return;
        }
        BoltOwnerData data = boltOwners.computeIfAbsent(owner, o -> new BoltOwnerData());
        data.repeat = newBoltData.shouldRepeat();
        double time = minecraft.world.getGameTime() + partialTicks;
        if (!data.repeat && time - data.lastBoltTimestamp >= data.lastBoltDelay) {
            data.addBolt(new BoltInstance(newBoltData), time);
        }
        data.lastUpdateTimestamp = time;
        data.lastBolt = newBoltData;
    }

    public class BoltOwnerData {

        private final Set<BoltInstance> bolts = new ObjectOpenHashSet<>();
        private BoltEffect lastBolt;
        private double lastBoltTimestamp;
        private double lastUpdateTimestamp;
        private double lastBoltDelay;
        private boolean repeat;

        private void addBolt(BoltInstance instance, double time) {
            bolts.add(instance);
            lastBoltDelay = instance.bolt.getSpawnFunction().getSpawnDelay(random);
            lastBoltTimestamp = time;
        }
    }

    public class BoltInstance {

        private final BoltEffect bolt;
        private final List<BoltQuads> renderQuads;
        private int ticksExisted;

        public BoltInstance(BoltEffect bolt) {
            this.bolt = bolt;
            this.renderQuads = bolt.generate();
        }

        public void render(Matrix4f matrix, IVertexBuilder buffer) {
            float lifeScale = (float) ticksExisted / bolt.getLifespan();
            Pair<Integer, Integer> bounds = bolt.getFadeFunction().getRenderBounds(renderQuads.size(), lifeScale);
            for (int i = bounds.getLeft(); i < bounds.getRight(); i++) {
                renderQuads.get(i).getVecs().forEach(v -> buffer.pos(matrix, (float) v.x, (float) v.y, (float) v.z)
                      .color(bolt.getColor().r, bolt.getColor().g, bolt.getColor().b, bolt.getColor().a)
                      .endVertex());
            }
        }

        public boolean tick() {
            return ticksExisted++ >= bolt.getLifespan();
        }
    }
}
