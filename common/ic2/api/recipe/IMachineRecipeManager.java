package ic2.api.recipe;

import java.util.Map;

import net.minecraft.item.ItemStack;

/**
 * Recipe manager interface for basic machines.
 * 
 * @author Richard
 */
public interface IMachineRecipeManager<V> {
	/**
	 * Adds a recipe to the machine.
	 * 
	 * @param input Recipe input
	 * @param output Recipe output
	 */
	public void addRecipe(ItemStack input, V output);
	
	/**
	 * Gets the recipe output for the given input.
	 * 
	 * @param input Recipe input
	 * @return Recipe output, or null if none
	 */
	public V getOutputFor(ItemStack input, boolean adjustInput);
	
	/**
	 * Gets a list of recipes.
	 * 
	 * You're a mad evil scientist if you ever modify this.
	 * 
	 * @return List of recipes
	 */
	public Map<ItemStack, V> getRecipes();
}
