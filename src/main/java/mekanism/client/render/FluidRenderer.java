package mekanism.client.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public final class FluidRenderer {

    private static final int BLOCK_STAGES = 1000;

    private static Map<RenderData, Int2ObjectMap<Model3D>> cachedCenterFluids = new Object2ObjectOpenHashMap<>();
    private static Map<ValveRenderData, Model3D> cachedValveFluids = new Object2ObjectOpenHashMap<>();

    public static Model3D getFluidModel(RenderData data, double fluidScale) {
        int maxStages = data.height * BLOCK_STAGES;
        int stage;
        if (data.fluidType.getFluid().getAttributes().isGaseous(data.fluidType)) {
            stage = maxStages;
        } else {
            stage = Math.min(maxStages, (int) (fluidScale * maxStages));
        }
        Int2ObjectMap<Model3D> cachedCenter;
        if (cachedCenterFluids.containsKey(data)) {
            cachedCenter = cachedCenterFluids.get(data);
            if (cachedCenter.containsKey(stage)) {
                return cachedCenter.get(stage);
            }
        } else {
            cachedCenterFluids.put(data, cachedCenter = new Int2ObjectOpenHashMap<>());
        }
        if (maxStages == 0) {
            maxStages = stage = 1;
        }

        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(data.fluidType, FluidType.STILL));

        cachedCenter.put(stage, model);
        model.minX = 0.01;
        model.minY = 0.01;
        model.minZ = 0.01;

        model.maxX = data.length - .01;
        model.maxY = ((float) stage / (float) maxStages) * data.height - .01;
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
        return data.valveLocation.y - data.location.y;
    }

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    public static class RenderData {

        public Coord4D location;

        public int height;
        public int length;
        public int width;

        @Nonnull
        public FluidStack fluidType = FluidStack.EMPTY;

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + location.hashCode();
            code = 31 * code + height;
            code = 31 * code + length;
            code = 31 * code + width;
            //TODO: Used to be name
            code = 31 * code + fluidType.getFluid().getRegistryName().hashCode();
            code = 31 * code + (fluidType.hasTag() ? fluidType.getTag().hashCode() : 0);
            return code;
        }

        @Override
        public boolean equals(Object data) {
            return data instanceof RenderData && ((RenderData) data).height == height && ((RenderData) data).length == length && ((RenderData) data).width == width
                   && ((RenderData) data).fluidType.isFluidEqual(fluidType);
        }
    }

    public static class ValveRenderData extends RenderData {

        public Direction side;
        public Coord4D valveLocation;

        public static ValveRenderData get(RenderData renderData, ValveData valveData) {
            ValveRenderData data = new ValveRenderData();

            data.location = renderData.location;
            data.height = renderData.height;
            data.length = renderData.length;
            data.width = renderData.width;
            data.fluidType = renderData.fluidType;

            data.side = valveData.side;
            data.valveLocation = valveData.location;

            return data;
        }

        @Override
        public boolean equals(Object data) {
            return data instanceof ValveRenderData && super.equals(data) && ((ValveRenderData) data).side.equals(side);
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + super.hashCode();
            code = 31 * code + side.ordinal();
            code = 31 * code + valveLocation.hashCode();
            return code;
        }
    }
}