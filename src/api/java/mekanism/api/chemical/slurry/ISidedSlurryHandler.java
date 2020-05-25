package mekanism.api.chemical.slurry;

import mekanism.api.chemical.ISidedChemicalHandler;

/**
 * A sided variant of {@link ISlurryHandler}
 */
public interface ISidedSlurryHandler extends ISidedChemicalHandler<Slurry, SlurryStack>, ISlurryHandler {
}