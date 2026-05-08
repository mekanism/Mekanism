package mekanism.common.capabilities.holder.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterChemicalTankHolder extends QuantumEntangloporterConfigHolder<IChemicalTank> implements IChemicalTankHolder {

    private final Function<InventoryFrequency, List<IChemicalTank>> tankResolver;
    private final TransmissionType transmissionType;

    public QuantumEntangloporterChemicalTankHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType,
          Function<InventoryFrequency, List<IChemicalTank>> tankResolver) {
        super(entangloporter);
        this.transmissionType = transmissionType;
        this.tankResolver = tankResolver;
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return transmissionType;
    }

    @NotNull
    @Override
    public List<IChemicalTank> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? tankResolver.apply(entangloporter.getFreq()) : Collections.emptyList();
    }
}