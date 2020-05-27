package mekanism.api.chemical.gas;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;

public interface IGasHandler extends IChemicalHandler<Gas, GasStack>, IEmptyGasProvider {

    /**
     * A sided variant of {@link IGasHandler}
     */
    interface ISidedGasHandler extends ISidedChemicalHandler<Gas, GasStack>, IGasHandler {
    }

    interface IMekanismGasHandler extends IMekanismChemicalHandler<Gas, GasStack, IGasTank>, ISidedGasHandler {
    }
}