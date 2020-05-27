package mekanism.api.chemical.infuse;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;

public interface IInfusionHandler extends IChemicalHandler<InfuseType, InfusionStack>, IEmptyInfusionProvider {

    /**
     * A sided variant of {@link IInfusionHandler}
     */
    interface ISidedInfusionHandler extends ISidedChemicalHandler<InfuseType, InfusionStack>, IInfusionHandler {
    }

    interface IMekanismInfusionHandler extends IMekanismChemicalHandler<InfuseType, InfusionStack, IInfusionTank>, ISidedInfusionHandler {
    }
}