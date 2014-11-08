package buildcraft.api.recipes;

import java.util.Collection;

/**
 * This class is intended for mods such as Not Enough Items
 * in order for them to be able to look inside a recipe.
 * 
 * It is intentionally left as a separate interface, so that
 * it remains possible to register a "dynamic" flexible
 * recipe which does not have static inputs and outputs.
 * 
 * @author asie
 */
public interface IFlexibleRecipeViewable {
	Object getOutput();
	
	/**
	 * With BuildCraft's implementation (as of 6.1.3), this might
	 * contain either an ItemStack, a List<ItemStack> or a FluidStack.
	 */
	Collection<Object> getInputs();
	
	long getCraftingTime();
	
	int getEnergyCost();
}
