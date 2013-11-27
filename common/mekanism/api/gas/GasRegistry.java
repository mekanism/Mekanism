package mekanism.api.gas;

import java.util.ArrayList;
import java.util.List;

public class GasRegistry
{
	private static ArrayList<Gas> registeredGasses = new ArrayList<Gas>();
	
	public static void registerOxygen()
	{
		if(getGas("oxygen") == null)
		{
			register(new Gas("oxygen").setLocalizedName("Oxygen"));
		}
	}
	
	public static void registerHydrogen()
	{
		if(getGas("hydrogen") == null)
		{
			register(new Gas("hydrogen").setLocalizedName("Hydrogen"));
		}
	}
	
	public static void register(Gas gas)
	{
		registeredGasses.add(gas);
	}
	
	public static Gas getGas(int id)
	{
		if(id == -1)
		{
			return null;
		}
		
		return registeredGasses.get(id);
	}
	
	public static boolean containsGas(String name)
	{
		return getGas(name) != null;
	}
	
	public static List<Gas> getRegisteredGasses()
	{
		return (List<Gas>)registeredGasses.clone();
	}
	
	public static Gas getGas(String name)
	{
		for(Gas gas : registeredGasses)
		{
			if(gas.getName().equals(name.toLowerCase()))
			{
				return gas;
			}
		}
		
		return null;
	}
	
	public static int getGasID(Gas gas)
	{
		if(gas == null || !containsGas(gas.getName()))
		{
			return -1;
		}
		
		return registeredGasses.indexOf(gas);
	}
}
