package mekanism.api.chemical.gas;

import mekanism.api.chemical.ISidedChemicalHandler;

/**
 * A sided variant of {@link IGasHandler}
 */
public interface ISidedGasHandler extends ISidedChemicalHandler<Gas, GasStack>, IGasHandler {
}