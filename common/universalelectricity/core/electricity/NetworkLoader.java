package universalelectricity.core.electricity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import universalelectricity.core.block.IConductor;
import universalelectricity.core.grid.IElectricityNetwork;
import cpw.mods.fml.common.FMLLog;

@SuppressWarnings("unchecked")
public class NetworkLoader
{
	/**
	 * The default IElectricityNetwork used for primary electrical networks.
	 */
	public static Class<? extends IElectricityNetwork> NETWORK_CLASS;
	public static final Set<Class<? extends IElectricityNetwork>> NETWORK_CLASS_REGISTRY = new HashSet<Class<? extends IElectricityNetwork>>();

	static
	{
		setNetworkClass("universalelectricity.core.grid.ElectricityNetwork");
	}

	public static void setNetworkClass(Class<? extends IElectricityNetwork> networkClass)
	{
		NETWORK_CLASS_REGISTRY.add(networkClass);
		NETWORK_CLASS = networkClass;
	}

	public static void setNetworkClass(String className)
	{
		try
		{
			setNetworkClass((Class<? extends IElectricityNetwork>) Class.forName(className));
		}
		catch (Exception e)
		{
			FMLLog.severe("Universal Electricity: Failed to set network class with name " + className);
			e.printStackTrace();
		}
	}

	public static IElectricityNetwork getNewNetwork(IConductor... conductors)
	{
		try
		{
			IElectricityNetwork network = NETWORK_CLASS.newInstance();
			network.getConductors().addAll(Arrays.asList(conductors));
			return network;
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}

}