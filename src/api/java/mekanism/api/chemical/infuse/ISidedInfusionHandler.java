package mekanism.api.chemical.infuse;

import mekanism.api.chemical.ISidedChemicalHandler;

/**
 * A sided variant of {@link IInfusionHandler}
 */
public interface ISidedInfusionHandler extends ISidedChemicalHandler<InfuseType, InfusionStack>, IInfusionHandler {
}