package mekanism.api.chemical.pigment;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;

public interface IPigmentHandler extends IChemicalHandler<Pigment, PigmentStack>, IEmptyPigmentProvider {

    /**
     * A sided variant of {@link IPigmentHandler}
     */
    interface ISidedPigmentHandler extends ISidedChemicalHandler<Pigment, PigmentStack>, IPigmentHandler {
    }

    interface IMekanismPigmentHandler extends IMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank>, ISidedPigmentHandler {
    }
}