package mekanism.common.capabilities.holder.energy;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterEnergyContainerHolder extends QuantumEntangloporterConfigHolder<IEnergyContainer> implements IEnergyContainerHolder {

    private final Lazy<List<IEnergyContainer>> clientContainer = Lazy.of(() -> Collections.singletonList(BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.getAsLong(), null)));

    public QuantumEntangloporterEnergyContainerHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (entangloporter.hasFrequency()) {
            return entangloporter.getFreq().getEnergyContainers(side);
        } else if (entangloporter.isRemote()) {
            return clientContainer.get();
        }
        return Collections.emptyList();
    }
}