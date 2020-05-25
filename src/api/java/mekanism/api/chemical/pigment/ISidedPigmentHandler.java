package mekanism.api.chemical.pigment;

import mekanism.api.chemical.ISidedChemicalHandler;

/**
 * A sided variant of {@link IPigmentHandler}
 */
public interface ISidedPigmentHandler extends ISidedChemicalHandler<Pigment, PigmentStack>, IPigmentHandler {
}