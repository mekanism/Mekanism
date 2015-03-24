package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * ICustomDamageItem allows items to have custom damage handling without the massive overhead of
 * Item.getDamage() / Item.setDamage(), which turn ItemStack.getItemDamage into virtual calls.
 *
 * The custom damage value is typically stored in the item stacks nbt data.
 *
 * If an Item implements ICusomDamageItem, the normal ItemStack damage won't be changed by IC2.
 * It's up to the implementer to potentially manipulate it for suitable visual effect on the
 * rendered damage bar or to provide a suitable item renderer.
 *
 * Item.isDamageable() still applies.
 *
 * @author Player
 */
public interface ICustomDamageItem {
	/**
	 * Retrieve the custom damage value for the supplied item stack.
	 *
	 * @param stack ItemStack to be queried.
	 * @return Custom damage value.
	 */
	int getCustomDamage(ItemStack stack);

	/**
	 * Retrieve the maximum custom damage value for the supplied item stack.
	 * @param stack ItemStack to be queried.
	 * @return Custom damage value limit.
	 */
	int getMaxCustomDamage(ItemStack stack);

	/**
	 * Set the custom damage value for the supplied item stack.
	 *
	 * @param stack ItemStack to be manipulated.
	 * @param damage New damage value.
	 */
	void setCustomDamage(ItemStack stack, int damage);

	/**
	 * Increase the custom damage value for the supplied item stack.
	 *
	 * It's up to the implementation to not apply any damage, e.g. based on some randomness or
	 * properties of src.
	 *
	 * @param stack ItemStack to be manipulated.
	 * @param damage Extra damage to be applied.
	 * @param src Entity damaging the item, may be null.
	 * @return true if damage was applied.
	 */
	boolean applyCustomDamage(ItemStack stack, int damage, EntityLivingBase src);
}
