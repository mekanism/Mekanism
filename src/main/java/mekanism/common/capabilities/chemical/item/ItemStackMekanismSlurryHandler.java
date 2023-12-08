package mekanism.common.capabilities.chemical.item;

import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing slurry handlers for items
 */
public abstract class ItemStackMekanismSlurryHandler extends ItemStackMekanismChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

    @SafeVarargs
    public ItemStackMekanismSlurryHandler(ItemStack stack, Function<IContentsListener, ISlurryTank>... tankProviders) {
        super(stack);
    }

    @NotNull
    @Override
    protected String getNbtKey() {
        return NBTConstants.SLURRY_TANKS;
    }
}