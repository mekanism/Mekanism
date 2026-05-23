package mekanism.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.data.RenderData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class ModelRenderer {

    private ModelRenderer() {
    }

    private static final int BLOCK_STAGES = FluidType.BUCKET_VOLUME;

    //TODO - 26.1: this can be replaced with an int key, packing the l,w,h into it
    private static final Map<RenderData, Int2ObjectMap<Model3D>> cachedCenterData = new Object2ObjectOpenHashMap<>();

    public static int getStage(FluidStack stack, int stages, double scale) {
        return getStage(MekanismUtils.lighterThanAirGas(stack), stages, scale);
    }

    public static int getStage(boolean gaseous, int stages, double scale) {
        if (gaseous) {
            return stages - 1;
        }
        return Math.min(stages - 1, (int) (scale * (stages - 1)));
    }

    /**
     * @apiNote If the data is gaseous then scale is ignored
     */
    public static Model3D getModel(RenderData data, double scale) {
        int maxStages = Math.max(data.height * BLOCK_STAGES, 1);
        int stage;
        if (data.height == 0) {
            //If there is no height set it to 1 for the stage as max stages is going to be one as well
            stage = 1;
        } else if (data.isGaseous()) {
            stage = maxStages;
        } else {
            stage = Math.min(maxStages, (int) (scale * maxStages));
        }
        Int2ObjectMap<Model3D> modelMap = cachedCenterData.computeIfAbsent(data, d -> new Int2ObjectOpenHashMap<>());
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .xBounds(0.01F, data.length - 0.02F)
                  .yBounds(0.01F, Math.max(0.02F, data.height * (stage / (float) maxStages) - 0.02F))
                  .zBounds(0.01F, data.width - 0.02F);
            modelMap.put(stage, model);
        }
        return model;
    }

    //todo: unsure if valveHeight and valveFluidHeight are actually different?? too much indirection in RenderData subclass
    public static boolean shouldSkipValveRender(float mainFluidHeight, Direction valveSide, int valveHeight, int valveFluidHeight) {
        return switch (valveSide) {
            case DOWN -> mainFluidHeight >= 0.49F;
            case UP -> mainFluidHeight >= valveHeight;
            default -> mainFluidHeight - valveFluidHeight >= 0.69F;
        };
    }

    public static void resetCachedModels() {
        cachedCenterData.clear();
    }
}