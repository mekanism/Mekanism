package ic2.api.recipe;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

public interface IRecipeInputFactory {
	IRecipeInput forStack(ItemStack stack);
	IRecipeInput forStack(ItemStack stack, int amount);
	IRecipeInput forOreDict(String name);
	IRecipeInput forOreDict(String name, int amount);
	IRecipeInput forOreDict(String name, int amount, int metaOverride);
	IRecipeInput forFluidContainer(Fluid fluid);
	IRecipeInput forFluidContainer(Fluid fluid, int amount);
	IRecipeInput forAny(IRecipeInput... options);
	IRecipeInput forAny(Iterable<IRecipeInput> options);
}
