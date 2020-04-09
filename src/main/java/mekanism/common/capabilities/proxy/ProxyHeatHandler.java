package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyHeatHandler extends ProxyHandler implements IHeatHandler {

    private final ISidedHeatHandler heatHandler;

    public ProxyHeatHandler(ISidedHeatHandler heatHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.heatHandler = heatHandler;
    }

    @Override
    public int getHeatCapacitorCount() {
        return heatHandler.getHeatCapacitorCount(side);
    }

    @Override
    public FloatingLong getTemperature(int capacitor) {
        return heatHandler.getTemperature(capacitor, side);
    }

    @Override
    public FloatingLong getInverseConduction(int capacitor) {
        return heatHandler.getInverseConduction(capacitor, side);
    }

    @Override
    public FloatingLong getInverseInsulation(int capacitor) {
        return heatHandler.getInverseInsulation(capacitor, side);
    }

    @Override
    public FloatingLong getHeatCapacity(int capacitor) {
        return heatHandler.getHeatCapacity(capacitor, side);
    }

    @Override
    public void handleHeat(int capacitor, HeatPacket transfer) {
        if (!readOnly) {
            heatHandler.handleHeat(capacitor, transfer, side);
        }
    }
}
