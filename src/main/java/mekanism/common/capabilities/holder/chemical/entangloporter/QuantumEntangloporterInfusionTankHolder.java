package mekanism.common.capabilities.holder.chemical.entangloporter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Direction;

public class QuantumEntangloporterInfusionTankHolder extends QuantumEntangloporterConfigHolder implements IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> {

    public QuantumEntangloporterInfusionTankHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.INFUSION;
    }

    @Nonnull
    @Override
    public List<IInfusionTank> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getInfusionTanks(side) : Collections.emptyList();
    }
}