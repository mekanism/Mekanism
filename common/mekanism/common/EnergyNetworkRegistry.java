package mekanism.common;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class EnergyNetworkRegistry implements ITickHandler
{
	private static EnergyNetworkRegistry INSTANCE = new EnergyNetworkRegistry();
	
	private HashSet<EnergyNetwork> networks = new HashSet<EnergyNetwork>();
	
	public EnergyNetworkRegistry()
	{
		TickRegistry.registerTickHandler(this, Side.SERVER);
	}
	
	public static EnergyNetworkRegistry getInstance()
	{
		return INSTANCE;
	}
	
	public void registerNetwork(EnergyNetwork network)
	{
		networks.add(network);
	}
	
	public void removeNetwork(EnergyNetwork network)
	{
		if(networks.contains(network))
		{
			networks.remove(network);
		}
	}
	
	public void pruneEmptyNetworks() {
		for(EnergyNetwork e : networks)
		{
			if(e.cables.size() == 0)
			{
				removeNetwork(e);
			}
		}
		
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		return;
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Set<EnergyNetwork> iterNetworks = (Set<EnergyNetwork>) networks.clone();
		for(EnergyNetwork net : iterNetworks)
		{
			if(networks.contains(net))
			{
				net.tick();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel()
	{
		return "Mekanism Energy Networks";
	}
	
	@Override
	public String toString() 
	{
		return networks.toString();
	}
}
