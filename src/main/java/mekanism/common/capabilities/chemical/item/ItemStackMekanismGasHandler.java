package mekanism.common.capabilities.chemical.item;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;

/**
 * Helper class for implementing gas handlers for items
 */
public abstract class ItemStackMekanismGasHandler extends ItemStackMekanismChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

    @Nonnull
    @Override
    protected String getNbtKey() {
        return NBTConstants.GAS_TANKS;
    }

    @Override
    protected void addCapabilityResolvers(@Nonnull CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER_CAPABILITY, this));
    }
}