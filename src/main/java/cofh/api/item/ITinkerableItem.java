package cofh.api.item;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes which can be externally modified through tinkering.
 * 
 * Basically a catch all for "upgrading" a given ItemStack, which may represent an Item or a Block.
 * 
 * @author King Lemming
 * 
 */
public interface ITinkerableItem {

	/**
	 * Returns a list of valid ItemStacks which may be used to upgrade this Tinkerable.
	 */
	List<ItemStack> getValidTinkers(ItemStack container);

	/**
	 * Applies a tinker to this item.
	 * 
	 * @param container
	 *            The ItemStack (Tinkerable) to which the tinker is being applied.
	 * @param tinker
	 *            The ItemStack representing the upgrade.
	 * @return True if the application was successful, false if it was not.
	 */
	boolean applyTinker(ItemStack container, ItemStack tinker);

}
