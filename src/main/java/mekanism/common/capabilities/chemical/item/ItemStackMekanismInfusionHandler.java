package mekanism.common.capabilities.chemical.item;

import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing infusion handlers for items
 */
public abstract class ItemStackMekanismInfusionHandler extends ItemStackMekanismChemicalHandler<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

    @SafeVarargs
    public ItemStackMekanismInfusionHandler(ItemStack stack, Function<IContentsListener, IInfusionTank>... tankProviders) {
        super(stack, tankProviders);
    }

    @NotNull
    @Override
    protected String getNbtKey() {
        return NBTConstants.INFUSION_TANKS;
    }
}