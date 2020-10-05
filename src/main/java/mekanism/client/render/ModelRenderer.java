package mekanism.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.ValveRenderData;

public final class ModelRenderer {

    private ModelRenderer() {
    }

    private static final int BLOCK_STAGES = 1_000;

    private static final Map<RenderData, Int2ObjectMap<Model3D>> cachedCenterData = new Object2ObjectOpenHashMap<>();
    private static final Map<ValveRenderData, Model3D> cachedValveFluids = new Object2ObjectOpenHashMap<>();

    /**
     * @apiNote If the data is gaseous then scale is ignored
     */
    public static Model3D getModel(RenderData data, double scale) {
        int maxStages = data.height * BLOCK_STAGES;
        int stage;
        if (data.isGaseous()) {
            stage = maxStages;
        } else {
            stage = Math.min(maxStages, (int) (scale * maxStages));
        }
        Int2ObjectMap<Model3D> cachedCenter;
        if (cachedCenterData.containsKey(data)) {
            cachedCenter = cachedCenterData.get(data);
            if (cachedCenter.containsKey(stage)) {
                return cachedCenter.get(stage);
            }
        } else {
            cachedCenterData.put(data, cachedCenter = new Int2ObjectOpenHashMap<>());
        }
        if (maxStages == 0) {
            maxStages = stage = 1;
        }

        Model3D model = new Model3D();
        model.setTexture(data.getTexture());

        cachedCenter.put(stage, model);
        model.minX = 0.01;
        model.minY = 0.01;
        model.minZ = 0.01;

        model.maxX = data.length - .01;
        model.maxY = (stage / (float) maxStages) * data.height - .01;
        model.maxZ = data.width - .01;
        return model;
    }

    public static Model3D getValveModel(ValveRenderData data) {
        if (cachedValveFluids.containsKey(data)) {
            return cachedValveFluids.get(data);
        }

        Model3D model = new Model3D();
        MekanismRenderer.prepFlowing(model, data.fluidType);
        cachedValveFluids.put(data, model);
        switch (data.side) {
            case DOWN:
                model.minX = 0.3;
                model.minY = 1.01;
                model.minZ = 0.3;

                model.maxX = 0.7;
                model.maxY = 1.5;
                model.maxZ = 0.7;
                break;
            case UP:
                model.minX = 0.3;
                model.minY = -data.height - 0.01;
                model.minZ = 0.3;

                model.maxX = 0.7;
                model.maxY = -0.01;
                model.maxZ = 0.7;
                break;
            case NORTH:
                model.minX = 0.3;
                model.minY = -getValveFluidHeight(data) + 0.01;
                model.minZ = 1.02;

                model.maxX = 0.7;
                model.maxY = 0.7;
                model.maxZ = 1.4;
                break;
            case SOUTH:
                model.minX = 0.3;
                model.minY = -getValveFluidHeight(data) + 0.01;
                model.minZ = -0.4;

                model.maxX = 0.7;
                model.maxY = 0.7;
                model.maxZ = -0.02;
                break;
            case WEST:
                model.minX = 1.02;
                model.minY = -getValveFluidHeight(data) + 0.01;
                model.minZ = 0.3;

                model.maxX = 1.4;
                model.maxY = 0.7;
                model.maxZ = 0.7;
                break;
            case EAST:
                model.minX = -0.4;
                model.minY = -getValveFluidHeight(data) + 0.01;
                model.minZ = 0.3;

                model.maxX = -0.02;
                model.maxY = 0.7;
                model.maxZ = 0.7;
                break;
            default:
                break;
        }
        return model;
    }

    private static int getValveFluidHeight(ValveRenderData data) {
        return data.valveLocation.getY() - data.location.getY();
    }

    public static void resetCachedModels() {
        cachedCenterData.clear();
        cachedValveFluids.clear();
    }
}