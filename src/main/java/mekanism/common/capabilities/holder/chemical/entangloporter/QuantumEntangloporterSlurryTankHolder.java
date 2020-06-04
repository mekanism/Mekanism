package mekanism.common.capabilities.holder.chemical.entangloporter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.util.Direction;

public class QuantumEntangloporterSlurryTankHolder extends QuantumEntangloporterConfigHolder implements IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> {

    public QuantumEntangloporterSlurryTankHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.SLURRY;
    }

    @Nonnull
    @Override
    public List<ISlurryTank> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getSlurryTanks(side) : Collections.emptyList();
    }
}