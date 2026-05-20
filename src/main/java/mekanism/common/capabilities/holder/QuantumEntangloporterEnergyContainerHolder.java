package mekanism.common.capabilities.holder;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterEnergyContainerHolder extends QuantumEntangloporterConfigHolder<IEnergyContainer> {

    private final Lazy<List<IEnergyContainer>> clientContainer = Lazy.of(() -> Collections.singletonList(BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.getAsLong(), null)));

    public QuantumEntangloporterEnergyContainerHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter, TransmissionType.ENERGY, InventoryFrequency::getEnergyContainers);
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getContainers(@Nullable Direction side) {
        List<IEnergyContainer> containers = super.getContainers(side);
        if (containers.isEmpty() && entangloporter.isRemote()) {
            return clientContainer.get();
        }
        return containers;
    }
}