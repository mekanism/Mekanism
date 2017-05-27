package ic2.api.recipe;

import net.minecraft.item.ItemStack;

import ic2.api.recipe.ICannerBottleRecipeManager.Input;
import ic2.api.recipe.ICannerBottleRecipeManager.RawInput;

public interface ICannerBottleRecipeManager extends IMachineRecipeManager<Input, ItemStack, RawInput> {
	/**
	 * Adds a recipe to the machine.
	 *
	 * @param container Container to be filled
	 * @param fill Item to fill into the container
	 * @param output Filled container
	 * @return
	 */
	boolean addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output, boolean replace);

	@Deprecated
	void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output);

	/**
	 * Gets the recipe output for the given input.
	 *
	 * @param container Container to be filled
	 * @param fill Item to fill into the container
	 * @param adjustInput modify the input according to the recipe's requirements
	 * @param acceptTest allow either container or fill to be null to see if either of them is part of a recipe
	 * @return Recipe output, or null if none
	 */
	@Deprecated
	RecipeOutput getOutputFor(ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptTest);

	public static class Input {
		public Input(IRecipeInput container, IRecipeInput fill) {
			this.container = container;
			this.fill = fill;
		}

		public boolean matches(ItemStack container, ItemStack fill) {
			return this.container.matches(container) && this.fill.matches(fill);
		}

		public final IRecipeInput container;
		public final IRecipeInput fill;
	}

	public static class RawInput {
		public RawInput(ItemStack container, ItemStack fill) {
			this.container = container;
			this.fill = fill;
		}

		public final ItemStack container;
		public final ItemStack fill;
	}
}
