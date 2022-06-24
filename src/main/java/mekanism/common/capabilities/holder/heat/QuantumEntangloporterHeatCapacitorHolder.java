package mekanism.common.capabilities.holder.heat;

import java.util.Collections;
import java.util.List;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterHeatCapacitorHolder extends QuantumEntangloporterConfigHolder<IHeatCapacitor> implements IHeatCapacitorHolder {

    public QuantumEntangloporterHeatCapacitorHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getHeatCapacitors(side) : Collections.emptyList();
    }
}
