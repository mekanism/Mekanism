package mekanism.client.render.data;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@NothingNullByDefault
public class ValveRenderData {

    private final Direction side;
    private final int valveFluidHeight;
    private final BlockPos valveLocation;
    public final float minX, minY, minZ;
    public final float maxX, maxY, maxZ;
    @RenderResizableCuboid.SideRender.SideRenderFlags
    public final byte renderCheck;

    private ValveRenderData(Direction side, BlockPos valveLocation, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, @RenderResizableCuboid.SideRender.SideRenderFlags byte renderCheck, BlockPos renderLocation) {
        //super(renderData.location, renderData.width, renderData.height, renderData.length, renderData.fluidType);
        this.side = side;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.renderCheck = renderCheck;
        this.valveFluidHeight = valveLocation.getY() - renderLocation.getY();
        this.valveLocation = valveLocation;
    }

    // mainFluidHeight is the model height, from getModel(renderdata)
    public static ValveRenderData get(ValveData valveData, BlockPos valveLocation, float mainFluidHeight, BlockPos renderLocation, int multiblockHeight) {
        byte renderCheck = RenderResizableCuboid.SideRender.ALL_FACES;
        if (mainFluidHeight == 0) {
            renderCheck = RenderResizableCuboid.SideRender.NOT_DOWN;
        }

        int valveFluidHeight = valveLocation.getY() - renderLocation.getY();

        float minX = 0.3F;
        float maxX = 0.7F;
        //Y defaults to horizonal facing values
        float minY = mainFluidHeight - valveFluidHeight + 0.01F;
        float maxY = 0.7F;
        float minZ = 0.3F;
        float maxZ = 0.7F;
        switch (valveData.side) {
            case DOWN -> {
                minY = mainFluidHeight + 1.01F;
                maxY = 1.5F;
            }
            case UP -> {
                minY = mainFluidHeight - multiblockHeight - 0.01F;
                maxY = -0.01F;
            }
            case NORTH -> {
                minZ = 1.02F;
                maxZ = 1.4F;
            }
            case SOUTH -> {
                minZ = -0.4F;
                maxZ = -0.03F;
            }
            case WEST -> {
                minX = 1.02F;
                maxX = 1.4F;
            }
            case EAST -> {
                minX = -0.4F;
                maxX = -0.03F;
            }
        }
        return new ValveRenderData(valveData.side, valveLocation, minX, minY, minZ, maxX, maxY, maxZ, renderCheck, renderLocation);
    }

    public int getValveFluidHeight() {
        return valveFluidHeight;
    }

    public Direction getSide() {
        return side;
    }

    @Override
    public boolean equals(Object data) {
        if (data == this) {
            return true;
        } else if (data == null) {
            return false;
        }
        return data.getClass() == ValveRenderData.class && /*equalsCommonFluid(data) && */side == ((ValveRenderData) data).side && valveFluidHeight == ((ValveRenderData) data).valveFluidHeight;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + side.hashCode();
        result = 31 * result + valveFluidHeight;
        return result;
    }

    public BlockPos getValveLocation() {
        return valveLocation;
    }
}