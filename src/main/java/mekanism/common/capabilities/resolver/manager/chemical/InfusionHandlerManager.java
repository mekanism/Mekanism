package mekanism.common.capabilities.resolver.manager.chemical;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.ISidedInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyInfusionHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class InfusionHandlerManager extends ChemicalHandlerManager<InfuseType, InfusionStack, IInfusionTank, IInfusionHandler, ISidedInfusionHandler> {

    public InfusionHandlerManager(@Nullable IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> holder, @Nonnull ISidedInfusionHandler baseHandler) {
        super(holder, baseHandler, Capabilities.INFUSION_HANDLER_CAPABILITY, ProxyInfusionHandler::new);
    }
}