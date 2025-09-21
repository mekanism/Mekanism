package mekanism.client.render.data;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@NothingNullByDefault
public class ValveRenderData extends FluidRenderData {

    private final Direction side;
    private final int valveFluidHeight;

    private ValveRenderData(FluidRenderData renderData, Direction side, BlockPos valveLocation) {
        super(renderData.location, renderData.width, renderData.height, renderData.length, renderData.fluidType);
        this.side = side;
        this.valveFluidHeight = valveLocation.getY() - location.getY();
    }

    public static ValveRenderData get(FluidRenderData renderData, ValveData valveData) {
        return new ValveRenderData(renderData, valveData.side, valveData.location);
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
        return data.getClass() == ValveRenderData.class && equalsCommonFluid(data) && side == ((ValveRenderData) data).side && valveFluidHeight == ((ValveRenderData) data).valveFluidHeight;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + side.hashCode();
        result = 31 * result + valveFluidHeight;
        return result;
    }
}