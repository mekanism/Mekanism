package ic2.api.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Interface for recipe ingredient matchers.
 *
 * See {@link Recipes#inputFactory} for some default factory methods.
 */
public interface IRecipeInput {
	/**
	 * Check if subject matches this recipe input, ignoring the amount.
	 *
	 * @param subject ItemStack to check
	 * @return true if it matches the requirement
	 */
	boolean matches(ItemStack subject);

	/**
	 * Determine the minimum input stack size.
	 *
	 * @return input amount required
	 */
	int getAmount();

	/**
	 * List all possible inputs (best effort).
	 *
	 * The stack size has to match getAmount().
	 *
	 * @return list of inputs, may be incomplete
	 */
	List<ItemStack> getInputs();
}
