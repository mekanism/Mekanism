package mekanism.common.attachments.containers.type;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.attachments.containers.heat.AttachedHeat;
import mekanism.common.attachments.containers.heat.HeatCapacitorData;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;

@NothingNullByDefault
public final class HeatContainerType extends AbstractContainerType<IHeatCapacitor, AttachedHeat> implements IListContainerType<IHeatCapacitor, AttachedHeat> {

    HeatContainerType() {
        super(MekanismDataComponents.ATTACHED_HEAT, SerializationConstants.HEAT_CAPACITORS, AttachedHeat.EMPTY);
    }

    @Override
    public List<IHeatCapacitor> getContainers(TileEntityMekanism tile) {
        return tile.getHeatCapacitors();
    }

    @Override
    public void copyToContainers(List<IHeatCapacitor> capacitors, AttachedHeat attached) {
        List<HeatCapacitorData> stored = attached.containers();
        int size = stored.size();
        if (size == capacitors.size()) {
            for (int i = 0; i < size; i++) {
                IHeatCapacitor capacitor = capacitors.get(i);
                HeatCapacitorData data = stored.get(i);
                if (data.heat().isPresent()) {
                    capacitor.setHeat(data.heat().getAsDouble());
                }
                if (capacitor instanceof BasicHeatCapacitor basic) {
                    basic.setHeatCapacity(data.capacity(), false);
                }
            }
        }
    }

    @Override
    public AttachedHeat attachedCopyOf(List<IHeatCapacitor> capacitors) {
        List<HeatCapacitorData> stored = new ArrayList<>(capacitors.size());
        for (IHeatCapacitor capacitor : capacitors) {
            if (capacitor.isAmbientTemperature()) {
                stored.add(new HeatCapacitorData(capacitor.getHeatCapacity()));
            } else {
                stored.add(new HeatCapacitorData(capacitor.getHeat(), capacitor.getHeatCapacity()));
            }
        }
        return new AttachedHeat(stored);
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleHeat();
    }

    @Override
    protected boolean shouldAddAttachment(AttachedHeat attached) {
        return !attached.isEmpty();
    }

    @Override
    public void copy(IHeatCapacitor from, IHeatCapacitor to) {
        to.copyContents(from);
    }
}