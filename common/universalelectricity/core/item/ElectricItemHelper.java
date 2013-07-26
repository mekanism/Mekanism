package universalelectricity.core.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Some helper functions for electric items.
 * 
 * @author Calclavia
 * 
 */
public class ElectricItemHelper
{
	/**
	 * Recharges an electric item.
	 * 
	 * @param joules - The joules being provided to the electric item
	 * @return The total amount of joules provided by the provider.
	 */
	public static float chargeItem(ItemStack itemStack, float joules)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				return ((IItemElectric) itemStack.getItem()).recharge(itemStack, Math.min(((IItemElectric) itemStack.getItem()).getTransfer(itemStack), joules), true);
			}
		}

		return 0;
	}

	/**
	 * Decharges an electric item.
	 * 
	 * @param joules - The joules being withdrawn from the electric item
	 * @return The total amount of joules the provider received.
	 */
	public static float dischargeItem(ItemStack itemStack, float joules)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				return ((IItemElectric) itemStack.getItem()).discharge(itemStack, Math.min(((IItemElectric) itemStack.getItem()).getMaxElectricityStored(itemStack), joules), true);
			}
		}

		return 0;
	}

	/**
	 * Returns an uncharged version of the electric item. Use this if you want the crafting recipe
	 * to use a charged version of the electric item instead of an empty version of the electric
	 * item
	 * 
	 * @return An electrical ItemStack with a specific charge.
	 */
	public static ItemStack getWithCharge(ItemStack itemStack, float joules)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				((IItemElectric) itemStack.getItem()).setElectricity(itemStack, joules);
				return itemStack;
			}
		}

		return itemStack;
	}

	public static ItemStack getWithCharge(Item item, float joules)
	{
		return getWithCharge(new ItemStack(item), joules);
	}

	public static ItemStack getCloneWithCharge(ItemStack itemStack, float joules)
	{
		return getWithCharge(itemStack.copy(), joules);
	}

	public static ItemStack getUncharged(ItemStack itemStack)
	{
		return getWithCharge(itemStack, 0);
	}

	public static ItemStack getUncharged(Item item)
	{
		return getUncharged(new ItemStack(item));
	}
}
