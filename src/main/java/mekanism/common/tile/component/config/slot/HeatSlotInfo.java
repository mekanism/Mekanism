package mekanism.common.tile.component.config.slot;

import mekanism.api.heat.IHeatCapacitor;
import org.jspecify.annotations.Nullable;

public class HeatSlotInfo extends BaseSlotInfo {

    @Nullable
    private final IHeatCapacitor capacitor;

    public HeatSlotInfo(boolean canInput, boolean canOutput, @Nullable IHeatCapacitor capacitor) {
        super(canInput, canOutput);
        this.capacitor = capacitor;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nullable
    public IHeatCapacitor getCapacitor() {
        return capacitor;
    }
}