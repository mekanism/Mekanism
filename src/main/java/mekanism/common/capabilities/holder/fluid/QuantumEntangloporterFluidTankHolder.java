package mekanism.common.capabilities.holder.fluid;

import java.util.Collections;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterFluidTankHolder extends QuantumEntangloporterConfigHolder<IExtendedFluidTank> implements IFluidTankHolder {

    public QuantumEntangloporterFluidTankHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.FLUID;
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? entangloporter.getFreq().getFluidTanks(side) : Collections.emptyList();
    }
}