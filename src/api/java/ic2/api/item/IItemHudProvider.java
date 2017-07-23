package ic2.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An interface to show that an {@link Item} can display the HUD overlay if worn.
 *
 * @author Chocohead
 */
public interface IItemHudProvider {
	/**
	 * Whether the given stack should display the HUD overlay.
	 *
	 * @param stack The stack currently being worn
	 * @return Whether the HUD overlay should be drawn
	 */
	boolean doesProvideHUD(ItemStack stack);

	/**
	 * Gets the current {@link HudMode} of the worn stack (probably stored as NBT)
	 *
	 * @param stack The stack currently being worn
	 * @return The HUD overlay mode
	 */
	HudMode getHudMode(ItemStack stack);

	/**
	 * <p>Override the percent shown by the HUD overlay for the applied item.</p>
	 *
	 * <p>By default, {@link IElectricItem}s have the percentage charged shown,
	 * damageable items have their {@link Item#getDurabilityForDisplay(ItemStack)} values shown
	 * and items that don't apply to either aren't shown at all.</p>
	 *
	 * @author Chocohead
	 */
	public static interface IItemHudBarProvider {
		/**
		 * Get the percent to show in the overlay HUD for the given stack.
		 *
		 * @param stack The stack to get the percent for.
		 * @return A number between 0 and 100, less than 0 will not render the item at all.
		 */
		int getBarPercent(ItemStack stack);
	}
}