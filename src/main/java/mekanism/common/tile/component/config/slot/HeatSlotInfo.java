package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.heat.IHeatCapacitor;

public class HeatSlotInfo extends BaseSlotInfo {

    private final List<IHeatCapacitor> capacitors;

    protected HeatSlotInfo(boolean canInput, boolean canOutput, IHeatCapacitor... capacitors) {
        this(canInput, canOutput, List.of(capacitors));
    }

    public HeatSlotInfo(boolean canInput, boolean canOutput, List<IHeatCapacitor> capacitors) {
        super(canInput, canOutput);
        this.capacitors = capacitors;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public List<IHeatCapacitor> getHeatCapacitors() {
        return capacitors;
    }
}