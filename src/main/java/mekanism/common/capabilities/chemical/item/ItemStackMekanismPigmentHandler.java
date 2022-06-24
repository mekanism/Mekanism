package mekanism.common.capabilities.chemical.item;

import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing pigment handlers for items
 */
public abstract class ItemStackMekanismPigmentHandler extends ItemStackMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

    @NotNull
    @Override
    protected String getNbtKey() {
        return NBTConstants.PIGMENT_TANKS;
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER, this));
    }
}