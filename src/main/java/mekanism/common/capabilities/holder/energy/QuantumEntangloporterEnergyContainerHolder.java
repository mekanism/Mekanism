package mekanism.common.capabilities.holder.energy;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Direction;

public class QuantumEntangloporterEnergyContainerHolder extends QuantumEntangloporterConfigHolder implements IEnergyContainerHolder {

    public QuantumEntangloporterEnergyContainerHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.frequency.getEnergyContainers(side) : Collections.emptyList();
    }
}