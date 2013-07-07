package mekanism.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;


public class EnergyNetworkRegistry implements ITickHandler
{

	public EnergyNetworkRegistry()
	{
		TickRegistry.registerTickHandler(this, Side.SERVER);
	}
	static private EnergyNetworkRegistry INSTANCE = new EnergyNetworkRegistry();
	
	static public EnergyNetworkRegistry getInstance()
	{
		return INSTANCE;
	}
	
	private Set<EnergyNetwork> networks = new HashSet<EnergyNetwork>();
	
	public void registerNetwork(EnergyNetwork network)
	{
		networks.add(network);
	}
	
	public void removeNetwork(EnergyNetwork network)
	{
		if (networks.contains(network))
		{
			networks.remove(network);
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
		for (EnergyNetwork net : networks)
		{
			net.clearJoulesTransmitted();
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
	
	public String toString() {
		return networks.toString();
	}
}
