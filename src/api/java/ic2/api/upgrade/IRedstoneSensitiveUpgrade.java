package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

/**
 * An interface to mark an item as an {@link UpgradableProperty#RedstoneSensitive} type upgrade
 *
 * @author Player, Chocohead
 */
public interface IRedstoneSensitiveUpgrade extends IUpgradeItem {
	/**
	 * Whether the upgrade modifies the given {@link IUpgradableBlock}'s redstone input
	 *
	 * @param stack The upgrade stack
	 * @param parent The block being modified
	 *
	 * @return Whether the redstone input should be changed (and thus {@link #getRedstoneInput(ItemStack, IUpgradableBlock, int)} be called)
	 */
	boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent);

	/**
	 * Change the redstone input given the {@link IUpgradableBlock}'s initial input value
	 *
	 * @param stack The upgrade stack
	 * @param parent The block receiving the redstone signal
	 * @param externalInput The initial redstone signal
	 *
	 * @return The new redstone signal
	 */
	int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput);
}