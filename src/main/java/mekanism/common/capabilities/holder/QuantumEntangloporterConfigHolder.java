package mekanism.common.capabilities.holder;

import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import org.jetbrains.annotations.Nullable;

public abstract class QuantumEntangloporterConfigHolder<TYPE> extends ConfigHolder<TYPE> {

    protected final TileEntityQuantumEntangloporter entangloporter;

    protected QuantumEntangloporterConfigHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
        this.entangloporter = entangloporter;
    }

    @Nullable
    public InventoryFrequency getFrequency() {
        return entangloporter.getFreq();
    }
}