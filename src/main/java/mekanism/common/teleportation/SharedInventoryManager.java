package mekanism.common.teleportation;

import java.util.HashMap;

public class SharedInventoryManager
{
	public static HashMap<String, SharedInventory> inventories = new HashMap<String, SharedInventory>();

	public static SharedInventory getInventory(String frequency)
	{
		if(frequency.length() <= 0)
		{
			return null;
		}

		SharedInventory inv = inventories.get(frequency);

		if(inv == null)
		{
			inv = new SharedInventory(frequency);

			inventories.put(frequency, inv);
		}

		return inv;
	}
}
