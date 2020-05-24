package mekanism.common.capabilities.resolver.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.ISidedSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxySlurryHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class SlurryHandlerManager extends CapabilityHandlerManager<IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank>, ISlurryTank, ISlurryHandler,
      ISidedSlurryHandler> {

    public SlurryHandlerManager(@Nullable IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> holder, @Nonnull ISidedSlurryHandler baseHandler) {
        super(holder, baseHandler, Capabilities.SLURRY_HANDLER_CAPABILITY, ProxySlurryHandler::new, IChemicalTankHolder::getTanks);
    }
}