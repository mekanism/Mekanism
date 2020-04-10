package mekanism.common.capabilities.holder;

import mekanism.common.tile.TileEntityQuantumEntangloporter;

public abstract class QuantumEntangloporterConfigHolder extends ConfigHolder {

    protected final TileEntityQuantumEntangloporter entangloporter;

    protected QuantumEntangloporterConfigHolder(TileEntityQuantumEntangloporter entangloporter) {
        super(entangloporter::getDirection, entangloporter::getConfig);
        this.entangloporter = entangloporter;
    }
}