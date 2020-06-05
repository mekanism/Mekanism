package mekanism.common.capabilities.holder.chemical.entangloporter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Direction;

public class QuantumEntangloporterPigmentTankHolder extends QuantumEntangloporterConfigHolder implements IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> {

    public QuantumEntangloporterPigmentTankHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.PIGMENT;
    }

    @Nonnull
    @Override
    public List<IPigmentTank> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getPigmentTanks(side) : Collections.emptyList();
    }
}