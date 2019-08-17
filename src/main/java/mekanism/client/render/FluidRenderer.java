package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.temporary.FluidRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public final class FluidRenderer {

    private static final int BLOCK_STAGES = 1000;

    private static Map<RenderData, DisplayInteger[]> cachedCenterFluids = new HashMap<>();
    private static Map<ValveRenderData, DisplayInteger> cachedValveFluids = new HashMap<>();

    public static void translateToOrigin(Coord4D origin) {
        GlStateManager.translatef((float) getX(origin.x), (float) getY(origin.y), (float) getZ(origin.z));
    }

    public static int getStages(RenderData data) {
        return data.height * BLOCK_STAGES;
    }

    public static DisplayInteger getTankDisplay(RenderData data) {
        return getTankDisplay(data, 1);
    }

    public static DisplayInteger getTankDisplay(RenderData data, double scale) {
        int maxStages = getStages(data);
        int stage = Math.min(maxStages, (int) (scale * maxStages));
        DisplayInteger[] cachedCenter;
        if (cachedCenterFluids.containsKey(data)) {
            cachedCenter = cachedCenterFluids.get(data);

            if (cachedCenter[stage] != null) {
                return cachedCenter[stage];
            }
        } else {
            cachedCenterFluids.put(data, cachedCenter = new DisplayInteger[maxStages + 1]);
        }
        if (maxStages == 0) {
            maxStages = stage = 1;
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = data.fluidType.getFluid().getBlock();
        if (toReturn.baseBlock == null) {
            toReturn.baseBlock = FluidRegistry.WATER.getBlock();
        }
        toReturn.setTexture(MekanismRenderer.getFluidTexture(data.fluidType, FluidType.STILL));

        DisplayInteger display = DisplayInteger.createAndStart();
        cachedCenter[stage] = display;

        if (data.fluidType.getFluid().getStill(data.fluidType) != null) {
            toReturn.minX = 0.01;
            toReturn.minY = 0.01;
            toReturn.minZ = 0.01;

            toReturn.maxX = data.length - .01;
            toReturn.maxY = ((float) stage / (float) maxStages) * data.height - .01;
            toReturn.maxZ = data.width - .01;

            MekanismRenderer.renderObject(toReturn);
        }

        GlStateManager.endList();

        return display;
    }

    public static DisplayInteger getValveDisplay(ValveRenderData data) {
        if (cachedValveFluids.containsKey(data)) {
            return cachedValveFluids.get(data);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.WATER;
        MekanismRenderer.prepFlowing(toReturn, data.fluidType);

        DisplayInteger display = DisplayInteger.createAndStart();
        cachedValveFluids.put(data, display);

        switch (data.side) {
            case DOWN:
                toReturn.minX = 0.3;
                toReturn.minY = 1.01;
                toReturn.minZ = 0.3;

                toReturn.maxX = 0.7;
                toReturn.maxY = 1.5;
                toReturn.maxZ = 0.7;
                break;
            case UP:
                toReturn.minX = 0.3;
                toReturn.minY = -data.height - 0.01;
                toReturn.minZ = 0.3;

                toReturn.maxX = 0.7;
                toReturn.maxY = -0.01;
                toReturn.maxZ = 0.7;
                break;
            case NORTH:
                toReturn.minX = 0.3;
                toReturn.minY = -getValveFluidHeight(data) + 0.01;
                toReturn.minZ = 1.02;

                toReturn.maxX = 0.7;
                toReturn.maxY = 0.7;
                toReturn.maxZ = 1.4;
                break;
            case SOUTH:
                toReturn.minX = 0.3;
                toReturn.minY = -getValveFluidHeight(data) + 0.01;
                toReturn.minZ = -0.4;

                toReturn.maxX = 0.7;
                toReturn.maxY = 0.7;
                toReturn.maxZ = -0.02;
                break;
            case WEST:
                toReturn.minX = 1.02;
                toReturn.minY = -getValveFluidHeight(data) + 0.01;
                toReturn.minZ = 0.3;

                toReturn.maxX = 1.4;
                toReturn.maxY = 0.7;
                toReturn.maxZ = 0.7;
                break;
            case EAST:
                toReturn.minX = -0.4;
                toReturn.minY = -getValveFluidHeight(data) + 0.01;
                toReturn.minZ = 0.3;

                toReturn.maxX = -0.02;
                toReturn.maxY = 0.7;
                toReturn.maxZ = 0.7;
                break;
            default:
                break;
        }

        if (data.fluidType.getFluid().getFlowing(data.fluidType) != null) {
            MekanismRenderer.renderObject(toReturn);
        }

        GlStateManager.endList();

        return display;
    }

    private static int getValveFluidHeight(ValveRenderData data) {
        return data.valveLocation.y - data.location.y;
    }

    private static double getX(int x) {
        return x - TileEntityRendererDispatcher.staticPlayerX;
    }

    private static double getY(int y) {
        return y - TileEntityRendererDispatcher.staticPlayerY;
    }

    private static double getZ(int z) {
        return z - TileEntityRendererDispatcher.staticPlayerZ;
    }

    public static void resetDisplayInts() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    public static class RenderData {

        public Coord4D location;

        public int height;
        public int length;
        public int width;

        public FluidStack fluidType;

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + location.hashCode();
            code = 31 * code + height;
            code = 31 * code + length;
            code = 31 * code + width;
            code = 31 * code + fluidType.getFluid().getName().hashCode();
            code = 31 * code + (fluidType.tag != null ? fluidType.tag.hashCode() : 0);
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