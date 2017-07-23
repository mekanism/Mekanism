package ic2.api.upgrade;

import java.util.Collection;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * An interface to mark an item as an upgrade for {@link IUpgradableBlock}s
 *
 * @author Player, Chocohead
 */
public interface IUpgradeItem {
	/**
	 * Checks if the upgrade is valid for the given {@link UpgradableProperty} types
	 *
	 * @param stack The upgrade stack being checked
	 * @param types The list of types being tested
	 *
	 * @return Whether the upgrade is valid
	 */
	boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types);

	/**
	 * Called every time the given {@link IUpgradableBlock} ticks
	 *
	 * @param stack The upgrade stack being ticked
	 * @param parent The parent block being ticked
	 *
	 * @return Whether to call {@link TileEntity#markDirty()} due to a stack changing
	 */
	boolean onTick(ItemStack stack, IUpgradableBlock parent);

	/**
	 * Called when the given {@link IUpgradableBlock} finishes processing
	 *
	 * @param stack The upgrade stack
	 * @param parent The parent finishing processing
	 * @param output The list of output products from the process, read only!
	 * @return final output
	 */
	Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock parent, Collection<ItemStack> output);
}