package universalelectricity.prefab.modifier;

import net.minecraft.item.ItemStack;

/**
 * This must be applied to an item that acts as a modifier or an upgrade.
 * 
 * @author Calclavia
 * 
 */
public interface IModifier
{
	/**
	 * @return - The name of the modifier.
	 */
	public String getName(ItemStack itemstack);

	/**
	 * @return - How much effect does this modifier have?
	 */
	public int getEffectiveness(ItemStack itemstack);
}
