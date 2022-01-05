package mekanism.common.capabilities.holder.energy;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import net.minecraft.core.Direction;

public class ConfigEnergyContainerHolder extends ConfigHolder<IEnergyContainer> implements IEnergyContainerHolder {

    public ConfigEnergyContainerHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addContainer(@Nonnull IEnergyContainer container) {
        slots.add(container);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof EnergySlotInfo info ? info.getContainers() : Collections.emptyList());
    }
}