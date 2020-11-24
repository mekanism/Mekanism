package mekanism.common.capabilities.chemical.item;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;

/**
 * Helper class for implementing slurry handlers for items
 */
public abstract class ItemStackMekanismSlurryHandler extends ItemStackMekanismChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

    @Nonnull
    @Override
    protected String getNbtKey() {
        return NBTConstants.SLURRY_TANKS;
    }

    @Override
    protected void addCapabilityResolvers(@Nonnull CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SLURRY_HANDLER_CAPABILITY, this));
    }
}