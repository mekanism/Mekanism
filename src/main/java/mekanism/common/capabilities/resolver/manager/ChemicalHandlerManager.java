package mekanism.common.capabilities.resolver.manager;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.ISidedChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
@ParametersAreNotNullByDefault
public class ChemicalHandlerManager extends CapabilityHandlerManager<IChemicalTankHolder, IChemicalTank, IChemicalHandler, ISidedChemicalHandler> {

    public ChemicalHandlerManager(@Nullable IChemicalTankHolder holder, ISidedChemicalHandler baseHandler) {
        super(holder, baseHandler, Capabilities.CHEMICAL.block(), ProxyChemicalHandler::new, IChemicalTankHolder::getTanks);
    }
}