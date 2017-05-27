package ic2.api.recipe;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Recipe manager interface for basic machines.
 *
 * @author Player, RichardG
 */
public interface IMachineRecipeManager<RI, RO, I> {
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
	 * @param output Recipe outputs, zero or more depending on the machine.
	 * @param metadata Meta data for additional recipe properties, may be null.
	 * @param replace Replace conflicting existing recipes, not recommended, may be ignored.
	 * @return true on success, false otherwise, e.g. on conflicts.
	 */
	boolean addRecipe(RI input, RO output, NBTTagCompound metadata, boolean replace);

	/**
	 * Gets the recipe result for the given input.
	 *
	 * @param input Recipe input (not modified)
	 * @param acceptTest If true the manager will accept partially missing ingredients or
	 * ingredients with insufficient quantities. This is primarily used to check whether a
	 * slot/tank/etc can accept the input while trying to supply a machine with resources.
	 * @return Recipe result, or null if none
	 */
	MachineRecipeResult<RI, RO, I> apply(I input, boolean acceptTest);

	/**
	 * Get all registered recipes (optional operation).
	 *
	 * The method is only available if {@link #isIterable()} is true.
	 * You're a mad evil scientist if you ever modify this.
	 *
	 * @return Iterable of all recipes registered to the manager.
	 * @throws UnsupportedOperationException if {@link #isIterable()} is false.
	 */
	Iterable<? extends MachineRecipe<RI, RO>> getRecipes();

	/**
	 * Determine whether the recipes can be iterated.
	 *
	 * @return true if {@link #getRecipes()} is implemented, false otherwise.
	 */
	boolean isIterable();
}
