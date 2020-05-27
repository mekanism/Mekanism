package mekanism.api.chemical.slurry;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;

public interface ISlurryHandler extends IChemicalHandler<Slurry, SlurryStack>, IEmptySlurryProvider {

    /**
     * A sided variant of {@link ISlurryHandler}
     */
    interface ISidedSlurryHandler extends ISidedChemicalHandler<Slurry, SlurryStack>, ISlurryHandler {
    }

    interface IMekanismSlurryHandler extends IMekanismChemicalHandler<Slurry, SlurryStack, ISlurryTank>, ISidedSlurryHandler {
    }
}