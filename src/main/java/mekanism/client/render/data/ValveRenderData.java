package mekanism.client.render.data;

import java.util.Objects;
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
        return data instanceof ValveRenderData other && super.equals(data) && side == other.side && valveFluidHeight == other.valveFluidHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), side, valveFluidHeight);
    }
}