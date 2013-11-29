package mekanism.api.gas;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.Fluid;

public class GasRegistry
{
	private static ArrayList<Gas> registeredGasses = new ArrayList<Gas>();
	
	public static Gas register(Gas gas)
	{
		if(gas == null)
		{
			return null;
		}
		
		registeredGasses.add(gas);
		
		return getGas(gas.getName());
	}
	
	public static Gas getGas(int id)
	{
		if(id == -1)
		{
			return null;
		}
		
		return registeredGasses.get(id);
	}
	
	public static Gas getGas(Fluid f)
	{
		for(Gas gas : getRegisteredGasses())
		{
			if(gas.hasFluid() && gas.getFluid() == f)
			{
				return gas;
			}
		}
		
		return null;
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
