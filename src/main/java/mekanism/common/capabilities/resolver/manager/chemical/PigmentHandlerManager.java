package mekanism.common.capabilities.resolver.manager.chemical;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.ISidedPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyPigmentHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class PigmentHandlerManager extends ChemicalHandlerManager<Pigment, PigmentStack, IPigmentTank, IPigmentHandler, ISidedPigmentHandler> {

    public PigmentHandlerManager(@Nullable IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> holder, @Nonnull ISidedPigmentHandler baseHandler) {
        super(holder, baseHandler, Capabilities.PIGMENT_HANDLER_CAPABILITY, ProxyPigmentHandler::new);
    }
}