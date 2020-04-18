package mekanism.api.chemical.gas;

import mekanism.api.chemical.IChemicalTank;

/**
 * Convenience extension to make working with generics easier.
 */
public interface IGasTank extends IChemicalTank<Gas, GasStack> {
}