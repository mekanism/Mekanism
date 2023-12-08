package mekanism.common.capabilities.chemical.item;

import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing pigment handlers for items
 */
public abstract class ItemStackMekanismPigmentHandler extends ItemStackMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

    @SafeVarargs
    public ItemStackMekanismPigmentHandler(ItemStack stack, Function<IContentsListener, IPigmentTank>... tankProviders) {
        super(stack, tankProviders);
    }

    @NotNull
    @Override
    protected String getNbtKey() {
        return NBTConstants.PIGMENT_TANKS;
    }
}