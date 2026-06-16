package mekanism.common.component.containers.type;

import mekanism.api.SerializationConstants;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.component.containers.heat.HeatCapacitorData;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class HeatContainerType extends AbstractContainerType<IHeatCapacitor, HeatCapacitorData> implements ISingleContainerType<IHeatCapacitor, HeatCapacitorData> {

    HeatContainerType() {
        super(MekanismDataComponents.ATTACHED_HEAT, SerializationConstants.HEAT_CAPACITOR);
    }

    @Nullable
    @Override
    public IHeatCapacitor getContainer(TileEntityMekanism tile) {
        return tile.getHeatCapacitor();
    }

    @Override
    public void copyToContainer(IHeatCapacitor capacitor, HeatCapacitorData data) {
        if (data.heat().isPresent()) {
            capacitor.setHeatAndCapacity(data.heat().getAsDouble(), data.capacity(), null);
        } else {
            capacitor.setHeatCapacity(data.capacity(), null);
        }
    }

    @Override
    public HeatCapacitorData attachedCopyOf(IHeatCapacitor capacitor) {
        if (capacitor.isAmbientTemperature()) {
            return new HeatCapacitorData(capacitor.getHeatCapacity());
        }
        return new HeatCapacitorData(capacitor.getHeat(), capacitor.getHeatCapacity());
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleHeat();
    }

    @Override
    public void copy(IHeatCapacitor from, IHeatCapacitor to, @Nullable TransactionContext transaction) {
        to.copyContents(from, transaction);
    }
}