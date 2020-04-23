package mekanism.client.render.bolt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.render.bolt.BoltEffect.BoltQuads;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.Vec3d;

public class BoltRenderer {

    private Random random = new Random();

    private Map<Object, Set<BoltInstance>> boltRenderMap = new Object2ObjectOpenHashMap<>();

    private final int newBoltRate;
    private final int boltLifespan;
    private final float FADE;

    private final BoltEffect boltEffect;

    private int renderTime;

    public static BoltRenderer create(BoltEffect boltEffect) {
        return create(boltEffect, 30, 60, 0.25F);
    }

    public static BoltRenderer create(BoltEffect boltEffect, int boltLifespan, int newBoltRate, float fade) {
        return new BoltRenderer(boltEffect, boltLifespan, newBoltRate, fade);
    }

    protected BoltRenderer(BoltEffect boltEffect, int boltLifespan, int newBoltRate, float fade) {
        this.boltEffect = boltEffect;
        this.boltLifespan = boltLifespan;
        this.newBoltRate = newBoltRate;
        this.FADE = fade;
    }

    public void render(Object renderer, Vec3d start, Vec3d end, int count, int segments, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();

        Set<BoltInstance> bolts = boltRenderMap.computeIfAbsent(renderer, (r) -> new HashSet<>());

        if (renderTime == 0) {
            renderTime = random.nextInt(newBoltRate) + 1;
            List<BoltQuads> quads = boltEffect.generate(start, end, count, segments);
            bolts.add(new BoltInstance(boltLifespan, quads));
        } else {
            renderTime--;
        }

        for (Iterator<BoltInstance> iter = bolts.iterator(); iter.hasNext();) {
            BoltInstance instance = iter.next();
            if (instance.render(matrix, buffer)) {
                iter.remove();
            }
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

        public boolean render(Matrix4f matrix, IVertexBuilder buffer) {
            float lifeScale = ticksExisted / lifespan;
            int start = 0 + lifeScale > (1 - FADE) ? (int) (renderQuads.size() * (lifeScale - (1 - FADE)) / FADE) : 0;
            int end = lifeScale < FADE ? (int) (renderQuads.size() * (lifeScale / FADE)) : renderQuads.size();
            for (int i = start; i < end; i++) {
                renderQuads.get(i).render(matrix, buffer, 1);
            }

            return ticksExisted++ >= lifespan;
        }
    }
}
