package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;


/**
 * Recipe manager interface for machines that will probably never be added.
 *
 * @author estebes, Player
 */
public interface IFluidRecipeManager {
	/**
	 * Adds a recipe to the machine.
	 *
	 * @param input Fluid input
	 * @param output Output fluid
	 */
	public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, FluidStack... output);

	/**
	 * Gets the recipe output for the given input.
	 *
	 * @param input Fluid input
	 * @param adjustInput modify the input according to the recipe's requirements
	 * @return Recipe output, or null if none, output fluid in nbt
	 */
	public RecipeOutputFluid getOutputFor(ItemStack input, boolean adjustInput);

	/**
	 * Get all registered recipes (optional operation).
	 *
	 * The method is only available if {@link #isIterable()} is true.
	 * You're a mad evil scientist if you ever modify this.
	 *
	 * @return Iterable of all recipes registered to the manager.
	 * @throws UnsupportedOperationException if {@link #isIterable()} is false.
	 */
	public Iterable<RecipeIoContainerFluid> getRecipes();

	/**
	 * Determine whether the recipes can be iterated.
	 *
	 * @return true if {@link #getRecipes()} is implemented, false otherwise.
	 */
	public boolean isIterable();


	public static class RecipeIoContainerFluid {
		public RecipeIoContainerFluid(IRecipeInput input, RecipeOutputFluid output) {
			this.input = input;
			this.output = output;
		}

		public final IRecipeInput input;
		public final RecipeOutputFluid output;
	}
}
