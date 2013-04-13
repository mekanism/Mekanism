package universalelectricity.core.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.electricity.ElectricityPack;

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
	 * @param voltage - The voltage in which is used to charge the electric item
	 * @return The total amount of joules provided by the provider.
	 */
	public static double chargeItem(ItemStack itemStack, double joules, double voltage)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) itemStack.getItem();
				double providingWatts = Math.min(joules, electricItem.getReceiveRequest(itemStack).getWatts());

				if (providingWatts > 0)
				{
					ElectricityPack providedElectricity = electricItem.onReceive(ElectricityPack.getFromWatts(providingWatts, voltage), itemStack);
					return providedElectricity.getWatts();
				}
			}
		}

		return 0;
	}

	/**
	 * Decharge an electric item.
	 * 
	 * @param joules - The joules being withdrawn from the electric item
	 * @param voltage - The voltage in which is used to decharge the electric item
	 * @return The total amount of joules the provider received.
	 */
	public static double dechargeItem(ItemStack itemStack, double joules, double voltage)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) itemStack.getItem();
				double requestingWatts = Math.min(joules, electricItem.getProvideRequest(itemStack).getWatts());

				if (requestingWatts > 0)
				{
					ElectricityPack receivedElectricity = electricItem.onProvide(ElectricityPack.getFromWatts(requestingWatts, voltage), itemStack);
					return receivedElectricity.getWatts();
				}
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
	public static ItemStack getWithCharge(ItemStack itemStack, double joules)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				((IItemElectric) itemStack.getItem()).setJoules(joules, itemStack);
				return itemStack;
			}
		}

		return itemStack;
	}

	public static ItemStack getWithCharge(Item item, double joules)
	{
		return getWithCharge(new ItemStack(item), joules);
	}

	public static ItemStack getCloneWithCharge(ItemStack itemStack, double joules)
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
