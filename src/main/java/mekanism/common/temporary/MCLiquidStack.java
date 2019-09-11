package mekanism.common.temporary;

import com.blamejared.crafttweaker.api.item.IItemStack;
import javax.annotation.Nonnull;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

//TODO: Remove when CrT has liquid support
public class MCLiquidStack implements ILiquidStack {

    @Nonnull
    private FluidStack stack;

    public MCLiquidStack(@Nonnull FluidStack fluidStack) {
        stack = fluidStack;
    }

    @Override
    public boolean matches(IItemStack stack) {
        return false;
    }

    @Override
    public Ingredient asVanillaIngredient() {
        return null;
    }

    @Override
    public String getCommandString() {
        return stack.getAmount() > 1 ? String.format("<liquid:%s> * %s", getName(), stack.getAmount()) : String.format("<liquid:%s>", getName());
    }

    @Override
    public IItemStack[] getItems() {
        return new IItemStack[0];
    }

    @Override
    public String getName() {
        return stack.getFluid().getRegistryName().toString();
    }

    @Override
    public int getAmount() {
        return stack.getAmount();
    }
}