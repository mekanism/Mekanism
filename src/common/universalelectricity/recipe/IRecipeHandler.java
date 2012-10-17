package universalelectricity.recipe;

import java.util.List;

public interface IRecipeHandler
{
	/**
	 * Called to add a recipe
	 * @param input - The input of the recipe
	 * @param output - The output of the recipe
	 */
	public void addRecipe(Object[] input, Object[] output);
	
	/**
	 * Gets a recipe's output by it's input
	 * @return - The output. Most likely an ItemStack.
	 */
	public Object getRecipeOutputByInput(Object[] input);
	
	/**
	 * Returns all recipes from this recipe handler
	 */
	public List getAllRecipes();
}
