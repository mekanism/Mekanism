package mekanism.common.content.teleportation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

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

		if(frequency.startsWith("creative."))
		{
			Iterator<String> creativeFreqs = Arrays.asList(frequency.substring(9).split("\\.")).iterator();
			while(creativeFreqs.hasNext())
			{
				String type = creativeFreqs.next();
				if(type.equals("energy"))
				{
					inv.MAX_ENERGY = Integer.MAX_VALUE;
					inv.setEnergy(Integer.MAX_VALUE);
				}
				else if(type.equals("fluid") && creativeFreqs.hasNext())
				{
					String fluidType = creativeFreqs.next();
					if(FluidRegistry.isFluidRegistered(fluidType))
					{
						inv.storedFluid.setCapacity(Integer.MAX_VALUE);
						inv.storedFluid.setFluid(new FluidStack(FluidRegistry.getFluid(fluidType), Integer.MAX_VALUE));
					}
				}
				else if(type.equals("gas") && creativeFreqs.hasNext())
				{
					String gasType = creativeFreqs.next();
					if(GasRegistry.containsGas(gasType))
					{
						inv.storedGas.setMaxGas(Integer.MAX_VALUE);
						inv.storedGas.setGas(new GasStack(GasRegistry.getGas(gasType), Integer.MAX_VALUE));
					}
				}
			}
		}

		return inv;
	}
}
