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
	 * @return - The type of the modifier.
	 */
	public String getType(ItemStack itemstack);

	/**
	 * @return - How much effect does this modifier have?
	 */
	public double getEffectiveness(ItemStack itemstack);
	
	/**
	 * @return - What UE tier is the Modifier?
	 */
	public int getTier(ItemStack itemstack);
}
