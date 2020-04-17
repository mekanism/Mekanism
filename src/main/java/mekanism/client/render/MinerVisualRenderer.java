package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public final class MinerVisualRenderer {

    private static final Map<MinerRenderData, Model3D> cachedVisuals = new Object2ObjectOpenHashMap<>();

    public static void resetCachedVisuals() {
        cachedVisuals.clear();
    }

    public static void render(@Nonnull TileEntityDigitalMiner miner, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer) {
        if (miner.getRadius() <= 64) {
            //TODO: Eventually we may want to make it so that the model can support each face being a different
            // color to make it easier to see the "depth"
            MekanismRenderer.renderObject(getModel(new MinerRenderData(miner)), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(255, 255, 255, 0.8F), MekanismRenderer.FULL_LIGHT);
        }
    }

    private static Model3D getModel(MinerRenderData data) {
        if (cachedVisuals.containsKey(data)) {
            return cachedVisuals.get(data);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.whiteIcon);
        model.minX = -data.radius + 0.01;
        model.minY = data.minY - data.yCoord + 0.01;
        model.minZ = -data.radius + 0.01;
        model.maxX = data.radius + 0.99;
        model.maxY = data.maxY - data.yCoord - 0.01;
        model.maxZ = data.radius + 0.99;
        return model;
    }

    public static class MinerRenderData {

        public int minY;
        public int maxY;
        public int radius;
        public int yCoord;

        public MinerRenderData(int min, int max, int rad, int y) {
            minY = min;
            maxY = max;
            radius = rad;
            yCoord = y;
        }

        public MinerRenderData(TileEntityDigitalMiner miner) {
            this(miner.getMinY(), miner.getMaxY(), miner.getRadius(), miner.getPos().getY());
        }

        @Override
        public boolean equals(Object data) {
            return data instanceof MinerRenderData && ((MinerRenderData) data).minY == minY && ((MinerRenderData) data).maxY == maxY &&
                   ((MinerRenderData) data).radius == radius && ((MinerRenderData) data).yCoord == yCoord;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + minY;
            code = 31 * code + maxY;
            code = 31 * code + radius;
            code = 31 * code + yCoord;
            return code;
        }
    }
}