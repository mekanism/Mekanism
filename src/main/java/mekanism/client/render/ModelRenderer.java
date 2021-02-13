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
        model.minX = 0.01F;
        model.minY = 0.01F;
        model.minZ = 0.01F;

        model.maxX = data.length - 0.02F;
        model.maxY = data.height * (stage / (float) maxStages) - 0.02F;
        model.maxZ = data.width - 0.02F;
        return model;
    }

    //Undoes the z-fighting height shift from the model
    public static float getActualHeight(Model3D model) {
        return model.maxY + 0.02F;
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
                model.minX = 0.3F;
                model.minY = 1.01F;
                model.minZ = 0.3F;

                model.maxX = 0.7F;
                model.maxY = 1.5F;
                model.maxZ = 0.7F;
                break;
            case UP:
                model.minX = 0.3F;
                model.minY = -data.height - 0.01F;
                model.minZ = 0.3F;

                model.maxX = 0.7F;
                model.maxY = -0.01F;
                model.maxZ = 0.7F;
                break;
            case NORTH:
                model.minX = 0.3F;
                model.minY = -getValveFluidHeight(data) + 0.02F;
                model.minZ = 1.02F;

                model.maxX = 0.7F;
                model.maxY = 0.7F;
                model.maxZ = 1.4F;
                break;
            case SOUTH:
                model.minX = 0.3F;
                model.minY = -getValveFluidHeight(data) + 0.02F;
                model.minZ = -0.4F;

                model.maxX = 0.7F;
                model.maxY = 0.7F;
                model.maxZ = -0.03F;
                break;
            case WEST:
                model.minX = 1.02F;
                model.minY = -getValveFluidHeight(data) + 0.02F;
                model.minZ = 0.3F;

                model.maxX = 1.4F;
                model.maxY = 0.7F;
                model.maxZ = 0.7F;
                break;
            case EAST:
                model.minX = -0.4F;
                model.minY = -getValveFluidHeight(data) + 0.02F;
                model.minZ = 0.3F;

                model.maxX = -0.03F;
                model.maxY = 0.7F;
                model.maxZ = 0.7F;
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