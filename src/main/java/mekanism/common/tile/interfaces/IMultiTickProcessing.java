package mekanism.common.tile.interfaces;

import mekanism.common.integration.computer.annotation.ComputerMethod;

public interface IMultiTickProcessing {

    @ComputerMethod(methodDescription = "Total number of ticks it takes currently processing operation to complete")
    int getTicksRequired();

    default int getOperationsPerTick() {
        return 1;
    }
}