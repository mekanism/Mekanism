package mekanism.client.render.data;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.IValveHandler.ValveData;
import net.minecraft.util.Direction;

public class ValveRenderData extends FluidRenderData {

    public Direction side;
    public Coord4D valveLocation;

    public static ValveRenderData get(FluidRenderData renderData, ValveData valveData) {
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
        int code = super.hashCode();
        code = 31 * code + side.ordinal();
        code = 31 * code + valveLocation.hashCode();
        return code;
    }
}