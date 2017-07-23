package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * This interface specifies a manager to handle the various tasks for electric items.
 *
 * The default implementation does the following:
 * - store and retrieve the charge
 * - handle charging, taking amount, tier, transfer limit, canProvideEnergy and simulate into account
 * - replace item IDs if appropriate (getChargedItemId() and getEmptyItemId())
 * - update and manage the damage value for the visual charge indicator
 *
 * Implementation hints:
 * - operations require the tier to be at least as high as the item being manipulated
 * - ignoreTransferLimit = true should allow the total charge to be transferred in one pass
 * - externally = false should always allow discharging
 * - all simulated operations should be deterministic and repeatable until a non-simulated
 *   operation happens or the item had a chance to be manipulated elsewhere.
 * - usually getCharge(stack) mirrors
 *   discharge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, true)
 * - usually getMaxCharge(stack) mirrors
 *   getCharge(stack) + charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true)
 *
 * @note If you're implementing your own variant (ISpecialElectricItem), you can delegate to the
 * default implementations through ElectricItem.rawManager. The default implementation is designed
 * to minimize its dependency on its own constraints/structure and delegates most work back to the
 * more atomic features in the gateway manager.
 */
public interface IElectricItemManager {
	/**
	 * Charge an item with a specified amount of energy.
	 *
	 * @param stack electric item's stack
	 * @param amount max amount of energy to charge in EU
	 * @param tier tier of the charging device, has to be at least as high as the item to charge
	 * @param ignoreTransferLimit ignore any transfer limits, infinite charge rate
	 * @param simulate don't actually change the item, just determine the return value
	 * @return Energy transferred into the electric item
	 */
	double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate);

	/**
	 * Discharge an item by a specified amount of energy
	 *
	 * The externally parameter is used to prevent non-battery-like items from providing power. For
	 * example discharge slots set externally to true, but items using energy for themselves don't.
	 * Special cases like the nano saber hitting armor will discharge with externally = false.
	 *
	 * @param stack electric item's stack
	 * @param amount max amount of energy to discharge in EU
	 * @param tier tier of the discharging device, has to be at least as high as the item to discharge
	 * @param ignoreTransferLimit ignore any transfer limits, infinite discharge rate
	 * @param externally use the supplied item externally, i.e. to power something else as if it was a battery
	 * @param simulate don't actually discharge the item, just determine the return value
	 * @return Energy retrieved from the electric item
	 */
	double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate);

	/**
	 * Determine the charge level for the specified item.
	 *
	 * @param stack ItemStack containing the electric item
	 * @return charge level in EU
	 */
	double getCharge(ItemStack stack);

	/**
	 * Determine the charge level for the specified item.
	 *
	 * The item may not actually be chargeable to the returned level, e.g. if it is a
	 * non-rechargeable single use battery.
	 *
	 * @param stack ItemStack containing the electric item
	 * @return maximum charge level in EU
	 */
	double getMaxCharge(ItemStack stack);

	/**
	 * Determine if the specified electric item has at least a specific amount of EU.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 * BatPacks are not taken into account.
	 *
	 * @param stack electric item's stack
	 * @param amount minimum amount of energy required
	 * @return true if there's enough energy
	 */
	boolean canUse(ItemStack stack, double amount);

	/**
	 * Try to retrieve a specific amount of energy from an Item, and if applicable, a BatPack.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 *
	 * @param stack electric item's stack
	 * @param amount amount of energy to discharge in EU
	 * @param entity entity holding the item, may be null
	 * @return true if the operation succeeded
	 */
	boolean use(ItemStack stack, double amount, EntityLivingBase entity);

	/**
	 * Charge an item from the BatPack a player is wearing.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 * use() already contains this functionality.
	 *
	 * @param stack electric item's stack
	 * @param entity entity holding the item
	 */
	void chargeFromArmor(ItemStack stack, EntityLivingBase entity);

	/**
	 * Get the tool tip to display for electric items.
	 *
	 * @param stack ItemStack to determine the tooltip for
	 * @return tool tip string or null for none
	 */
	String getToolTip(ItemStack stack);

	/**
	 * Get the tier of the specified item.
	 * @param stack the item to get the tier from.
	 * @return The tier of the item.
	 */
	int getTier(ItemStack stack);
}
