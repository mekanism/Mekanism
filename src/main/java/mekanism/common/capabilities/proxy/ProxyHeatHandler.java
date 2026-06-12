package mekanism.common.capabilities.proxy;

import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ProxyHeatHandler extends ProxyHandler<ISingleContainerHolder<IHeatCapacitor>> implements IHeatHandler {


    public ProxyHeatHandler(@Nullable Direction side, ISingleContainerHolder<IHeatCapacitor> holder) {
        super(side, holder);
    }

    @Nullable
    private IHeatCapacitor getHeatCapacitor() {
        return holder.getContainer(side);
    }

    @Override
    public double getTemperature() {
        IHeatCapacitor heatCapacitor = getHeatCapacitor();
        return heatCapacitor == null ? HeatAPI.AMBIENT_TEMP : heatCapacitor.getTemperature();
    }

    @Override
    public double getInverseConduction() {
        IHeatCapacitor heatCapacitor = getHeatCapacitor();
        return heatCapacitor == null ? HeatAPI.DEFAULT_INVERSE_CONDUCTION : heatCapacitor.getInverseConduction();
    }

    @Override
    public double getHeatCapacity() {
        IHeatCapacitor heatCapacitor = getHeatCapacitor();
        return heatCapacitor == null ? HeatAPI.DEFAULT_HEAT_CAPACITY : heatCapacitor.getHeatCapacity();
    }

    @Override
    public void handleHeat(double transfer, TransactionContext transaction) {
        if (!readOnly) {
            if (transfer > 0 && readOnlyInsert()) {
                return;
            } else if (transfer < 0 && readOnlyExtract()) {
                return;
            }
            IHeatCapacitor heatCapacitor = getHeatCapacitor();
            if (heatCapacitor != null) {
                heatCapacitor.handleHeat(transfer, transaction);
            }
        }
    }
}
