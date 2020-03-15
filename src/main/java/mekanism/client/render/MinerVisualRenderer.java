package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public final class MinerVisualRenderer {

    private static Map<MinerRenderData, List<Model3D>> cachedVisuals = new Object2ObjectOpenHashMap<>();

    public static void resetCachedVisuals() {
        cachedVisuals.clear();
    }

    public static void render(@Nonnull TileEntityDigitalMiner miner, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer) {
        List<Model3D> models = getModels(new MinerRenderData(miner));
        IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
        int argb = MekanismRenderer.getColorARGB(1, 1, 1, 0.8F);
        //TODO: Rendering the visuals drops FPS by a good bit, can we at least batch getting the vertex builder
        // Or maybe we should just make one large square?
        for (Model3D model : models) {
            MekanismRenderer.renderObject(model, matrix, buffer, argb, MekanismRenderer.FULL_LIGHT);
        }
    }

    private static List<Model3D> getModels(MinerRenderData data) {
        if (cachedVisuals.containsKey(data)) {
            return cachedVisuals.get(data);
        }
        List<Model3D> models = new ArrayList<>();
        cachedVisuals.put(data, models);
        if (data.radius <= 64) {
            for (int x = -data.radius; x <= data.radius; x++) {
                for (int y = data.minY - data.yCoord; y <= data.maxY - data.yCoord; y++) {
                    for (int z = -data.radius; z <= data.radius; z++) {
                        if (x == -data.radius || x == data.radius || y == data.minY - data.yCoord || y == data.maxY - data.yCoord || z == -data.radius || z == data.radius) {
                            Model3D model = new Model3D();
                            model.setBlockBounds(x + 0.4, y + 0.4, z + 0.4, x + 0.6, y + 0.6, z + 0.6);
                            model.setTexture(MekanismRenderer.whiteIcon);
                            models.add(model);
                        }
                    }
                }
            }
        }
        return models;
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
            this(miner.minY, miner.maxY, miner.getRadius(), miner.getPos().getY());
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