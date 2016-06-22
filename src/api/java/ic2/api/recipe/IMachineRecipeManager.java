package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Recipe manager interface for basic machines.
 *
 * @author Player, RichardG
 */
public interface IMachineRecipeManager {
	/**
	 * Adds a recipe to the machine.
	 *
	 * Meta data format:
	 * - thermal centrifuge: {"minHeat": 1-xxx}
	 * - ore washing plant: {"amount": 1-8000}
	 *
	 * @note Replace is only as reliable as IRecipeInput.getInputs().
	 *
	 * @param input Recipe input
	 * @param metadata Meta data for additional recipe properties, may be null.
	 * @param replace Replace conflicting existing recipes, not recommended, may be ignored.
	 * @param outputs Recipe outputs, zero or more depending on the machine.
	 * @return true on success, false otherwise, e.g. on conflicts.
	 */
	public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs);

	/**
	 * Gets the recipe output for the given input.
	 *
	 * @param input Recipe input
	 * @param adjustInput modify the input according to the recipe's requirements
	 * @return Recipe output, or null if none
	 */
	public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput);

	/**
	 * Get all registered recipes (optional operation).
	 *
	 * The method is only available if {@link #isIterable()} is true.
	 * You're a mad evil scientist if you ever modify this.
	 *
	 * @return Iterable of all recipes registered to the manager.
	 * @throws UnsupportedOperationException if {@link #isIterable()} is false.
	 */
	public Iterable<RecipeIoContainer> getRecipes();

	/**
	 * Determine whether the recipes can be iterated.
	 *
	 * @return true if {@link #getRecipes()} is implemented, false otherwise.
	 */
	public boolean isIterable();


	public static class RecipeIoContainer {
		public RecipeIoContainer(IRecipeInput input, RecipeOutput output) {
			this.input = input;
			this.output = output;
		}

		public final IRecipeInput input;
		public final RecipeOutput output;
	}
}
