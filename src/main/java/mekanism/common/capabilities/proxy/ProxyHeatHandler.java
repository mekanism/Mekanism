package mekanism.common.capabilities.proxy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
    public double getTemperature(int capacitor) {
        return heatHandler.getTemperature(capacitor, side);
    }

    @Override
    public double getInverseConduction(int capacitor) {
        return heatHandler.getInverseConduction(capacitor, side);
    }

    @Override
    public double getHeatCapacity(int capacitor) {
        return heatHandler.getHeatCapacity(capacitor, side);
    }

    @Override
    public void handleHeat(int capacitor, double transfer) {
        if (!readOnly) {
            heatHandler.handleHeat(capacitor, transfer, side);
        }
    }

    @Override
    public double getTotalTemperature() {
        return heatHandler.getTotalTemperature(side);
    }

    @Override
    public double getTotalInverseConduction() {
        return heatHandler.getTotalInverseConductionCoefficient(side);
    }

    @Override
    public double getTotalHeatCapacity() {
        return heatHandler.getTotalHeatCapacity(side);
    }

    @Override
    public void handleHeat(double transfer) {
        if (!readOnly) {
            heatHandler.handleHeat(transfer, side);
        }
    }
}
