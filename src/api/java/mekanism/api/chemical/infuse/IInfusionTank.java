package mekanism.api.chemical.infuse;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.IChemicalTank;

/**
 * Convenience extension to make working with generics easier.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInfusionTank extends IChemicalTank<InfuseType, InfusionStack> {

    @Override
    default InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }

    @Override
    default InfusionStack createStack(InfusionStack stored, long size) {
        return new InfusionStack(stored, size);
    }
}