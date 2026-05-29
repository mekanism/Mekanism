package mekanism.common.tile.component.config.slot;

import mekanism.api.energy.IEnergyContainer;
import org.jspecify.annotations.Nullable;

public class EnergySlotInfo extends BaseSlotInfo {

    @Nullable
    private final IEnergyContainer container;

    public EnergySlotInfo(boolean canInput, boolean canOutput, @Nullable IEnergyContainer container) {
        super(canInput, canOutput);
        this.container = container;
    }

    @Override
    public boolean isEmpty() {
        IEnergyContainer container = getContainer();
        return container == null || container.isEmpty();
    }

    @Nullable
    public IEnergyContainer getContainer() {
        return container;
    }
}