package mekanism.common.capabilities.holder;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumEntangloporterConfigHolder<TYPE> extends ConfigHolder<TYPE> {

    private final Function<InventoryFrequency, List<TYPE>> containerResolver;
    protected final TileEntityQuantumEntangloporter entangloporter;

    public QuantumEntangloporterConfigHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType,
          Function<InventoryFrequency, List<TYPE>> containerResolver) {
        //TODO - 26.1: Re-evaluate this, does passing null mean the side config is ignored, or is that handled somewhere else?
        super(entangloporter, transmissionType, null);
        this.entangloporter = entangloporter;
        this.containerResolver = containerResolver;
    }

    @NotNull
    @Override
    public List<TYPE> getContainers(@Nullable Direction side) {
        return entangloporter.hasFrequency() ? containerResolver.apply(entangloporter.getFreq()) : Collections.emptyList();
    }
}