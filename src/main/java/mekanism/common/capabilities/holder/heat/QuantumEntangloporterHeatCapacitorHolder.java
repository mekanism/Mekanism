package mekanism.common.capabilities.holder.heat;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Direction;

public class QuantumEntangloporterHeatCapacitorHolder extends QuantumEntangloporterConfigHolder implements IHeatCapacitorHolder {

    public QuantumEntangloporterHeatCapacitorHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getHeatCapacitors(side) : Collections.emptyList();
    }
}
