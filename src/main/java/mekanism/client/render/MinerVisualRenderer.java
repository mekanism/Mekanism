package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.inventory.container.PlayerContainer;

public final class MinerVisualRenderer {

    private static Map<MinerRenderData, List<Model3D>> cachedVisuals = new HashMap<>();

    public static void resetCachedVisuals() {
        cachedVisuals.clear();
    }

    public static void render(@Nonnull TileEntityDigitalMiner miner, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer) {
        matrix.func_227860_a_();
        List<Model3D> models = getModels(new MinerRenderData(miner));
        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        RenderType.State.Builder stateBuilder = MekanismRenderType.configurableMachineState(PlayerContainer.field_226615_c_);
        //TODO: Rendering the visuals drops FPS by a good bit, can we at least batch getting the vertex builder
        // Or maybe we should just make one large square?
        for (Model3D model : models) {
            MekanismRenderer.renderObject(model, matrix, renderer, stateBuilder, MekanismRenderer.getColorARGB(1, 1, 1, 0.8F));
        }
        MekanismRenderer.disableGlow(glowInfo);
        matrix.func_227865_b_();
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
                            model.baseBlock = Blocks.WATER;
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