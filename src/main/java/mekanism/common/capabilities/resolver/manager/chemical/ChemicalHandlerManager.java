package mekanism.common.capabilities.resolver.manager.chemical;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.manager.CapabilityHandlerManager;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Helper class to make reading instead of having as messy generics
 */
@ParametersAreNonnullByDefault
public class ChemicalHandlerManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
      HANDLER extends IChemicalHandler<CHEMICAL, STACK>, SIDED_HANDLER extends HANDLER> extends CapabilityHandlerManager<IChemicalTankHolder<CHEMICAL, STACK, TANK>,
      TANK, HANDLER, SIDED_HANDLER> {

    protected ChemicalHandlerManager(@Nullable IChemicalTankHolder<CHEMICAL, STACK, TANK> holder, SIDED_HANDLER baseHandler, Capability<HANDLER> supportedCapability,
          ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator) {
        super(holder, baseHandler, supportedCapability, proxyCreator, IChemicalTankHolder::getTanks);
    }
}