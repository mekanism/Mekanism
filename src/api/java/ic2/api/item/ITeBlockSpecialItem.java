package ic2.api.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

import ic2.api.info.ITeBlock;

/**
 *  Change the default icon for an {@link ITeBlock} by implementing this.
 *
 *	@since IC2 2.6.85
 *	@version 1.0
 *
 *	@see {@link ITeBlock} for more information
 *
 *  @author Chocohead
 */
public interface ITeBlockSpecialItem {
	/**
	 * Should the model be delegated to the {@link ITeBlock} or the default used?
	 *
	 * @param stack The itemstack being rendered
	 * @return Whether the {@link ITeBlock} will return an {@link ModelResourceLocation}
	 */
	boolean doesOverrideDefault(ItemStack stack);

	/**
	 * The {@link ModelResourceLocation} for the given stack.
	 * Will still use the default even if {@link #doesOverrideDefault(ItemStack)} returns <tt>true</tt> if this returns <tt>null</tt>.
	 *
	 * @param stack The itemstack being rendered
	 * @return The model location to be used
	 */
	ModelResourceLocation getModelLocation(ItemStack stack);
}