package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class ValveRenderData extends FluidRenderData {

    public Direction side;
    public BlockPos valveLocation;

    private ValveRenderData(@Nonnull FluidStack fluidType) {
        super(fluidType);
    }

    public static ValveRenderData get(FluidRenderData renderData, ValveData valveData) {
        ValveRenderData data = new ValveRenderData(renderData.fluidType);
        data.location = renderData.location;
        data.height = renderData.height;
        data.length = renderData.length;
        data.width = renderData.width;
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
        int code = super.hashCode();
        code = 31 * code + side.ordinal();
        code = 31 * code + valveLocation.hashCode();
        return code;
    }
}