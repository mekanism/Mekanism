package mekanism.common.temporary;

import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

//TODO: Remove when CrT has liquid support
public class MCLiquidStack implements ILiquidStack {

    private FluidStack stack;

    public MCLiquidStack(FluidStack fluidStack) {
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
        return stack.amount > 1 ? String.format("<gas:%s> * %s", getName(), stack.amount) : String.format("<gas:%s>", getName());
    }

    @Override
    public IItemStack[] getItems() {
        return new IItemStack[0];
    }

    @Override
    public String getName() {
        return stack.getFluid().getName();
    }

    @Override
    public int getAmount() {
        return stack.amount;
    }
}