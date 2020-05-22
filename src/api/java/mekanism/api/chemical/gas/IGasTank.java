package mekanism.api.chemical.gas;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.IChemicalTank;

/**
 * Convenience extension to make working with generics easier.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IGasTank extends IChemicalTank<Gas, GasStack> {

    @Override
    default GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }

    @Override
    default GasStack createStack(GasStack stored, long size) {
        return new GasStack(stored, size);
    }
}