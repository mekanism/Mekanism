package mekanism.common.capabilities.holder.chemical;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.QuantumEntangloporterConfigHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>> extends QuantumEntangloporterConfigHolder<TANK> implements IChemicalTankHolder<CHEMICAL, STACK, TANK> {

    private final BiFunction<InventoryFrequency, Direction, List<TANK>> tankResolver;
    private final TransmissionType transmissionType;

    public QuantumEntangloporterChemicalTankHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType,
          BiFunction<InventoryFrequency, Direction, List<TANK>> tankResolver) {
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
    public List<TANK> getTanks(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? tankResolver.apply(entangloporter.getFreq(), side) : Collections.emptyList();
    }
}