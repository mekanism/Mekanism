package mekanism.common.capabilities.chemical.item;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;

/**
 * Helper class for implementing pigment handlers for items
 */
public abstract class ItemStackMekanismPigmentHandler extends ItemStackMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

    @Nonnull
    @Override
    protected String getNbtKey() {
        return NBTConstants.PIGMENT_TANKS;
    }

    @Override
    protected void addCapabilityResolvers(@Nonnull CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER_CAPABILITY, this));
    }
}