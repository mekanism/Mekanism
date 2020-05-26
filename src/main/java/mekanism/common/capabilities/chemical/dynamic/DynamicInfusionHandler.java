package mekanism.common.capabilities.chemical.dynamic;

import mekanism.api.IContentsListener;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraftforge.common.util.NonNullSupplier;

public class DynamicInfusionHandler extends DynamicChemicalHandler<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

    public DynamicInfusionHandler(NonNullSupplier<ChemicalHandlerManager<InfuseType, InfusionStack, IInfusionTank, ?, ?>> handlerManager, InteractPredicate canExtract,
          InteractPredicate canInsert, IContentsListener listener) {
        super(handlerManager, canExtract, canInsert, listener);
    }
}