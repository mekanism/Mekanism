package mekanism.api.chemical.slurry;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.IChemicalTank;

/**
 * Convenience extension to make working with generics easier.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISlurryTank extends IChemicalTank<Slurry, SlurryStack> {

    @Override
    default SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }

    @Override
    default SlurryStack createStack(SlurryStack stored, long size) {
        return new SlurryStack(stored, size);
    }
}