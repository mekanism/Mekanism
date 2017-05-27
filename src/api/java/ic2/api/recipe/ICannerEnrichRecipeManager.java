package ic2.api.recipe;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import ic2.api.recipe.ICannerEnrichRecipeManager.Input;
import ic2.api.recipe.ICannerEnrichRecipeManager.RawInput;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<Input, FluidStack, RawInput> {
	/**
	 * Adds a recipe to the machine.
	 *
	 * @param input Fluid input
	 * @param additive Item to enrich the fluid with
	 * @param output Output fluid
	 */
	@Deprecated
	public void addRecipe(FluidStack input, IRecipeInput additive, FluidStack output);

	/**
	 * Gets the recipe output for the given input.
	 *
	 * @param input Fluid input
	 * @param additive Item to enrich the fluid with
	 * @param adjustInput modify the input according to the recipe's requirements
	 * @param acceptTest allow input or additive to be null to see if either of them is part of a recipe
	 * @return Recipe output, or null if none, output fluid in nbt
	 */
	@Deprecated
	public RecipeOutput getOutputFor(FluidStack input, ItemStack additive, boolean adjustInput, boolean acceptTest);

	public static class Input {
		public Input(FluidStack fluid, IRecipeInput additive) {
			this.fluid = fluid;
			this.additive = additive;
		}

		public boolean matches(FluidStack fluid, ItemStack additive) {
			return this.fluid.isFluidEqual(fluid) && this.additive.matches(additive);
		}

		public final FluidStack fluid;
		public final IRecipeInput additive;
	}

	public static class RawInput {
		public RawInput(FluidStack fluid, ItemStack additive) {
			this.fluid = fluid;
			this.additive = additive;
		}

		public final FluidStack fluid;
		public final ItemStack additive;
	}
}
