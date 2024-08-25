package mekanism.common.capabilities.holder;

import mekanism.common.tile.TileEntityQuantumEntangloporter;

public abstract class QuantumEntangloporterConfigHolder<TYPE> extends ConfigHolder<TYPE> {

    protected final TileEntityQuantumEntangloporter entangloporter;

    protected QuantumEntangloporterConfigHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter);
        this.entangloporter = entangloporter;
    }
}