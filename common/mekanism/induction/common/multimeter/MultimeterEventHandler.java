package mekanism.induction.common.multimeter;

import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import universalelectricity.core.electricity.ElectricalEvent.ElectricityProductionEvent;
import universalelectricity.core.grid.IElectricityNetwork;

/**
 * @author Calclavia
 * 
 */
public class MultimeterEventHandler
{
	private static final HashMap<IElectricityNetwork, Float> networkEnergyCache = new HashMap<IElectricityNetwork, Float>();
	private static long lastCheckTime = 0;

	public static HashMap<IElectricityNetwork, Float> getCache(World worldObj)
	{
		HashMap<IElectricityNetwork, Float> returnCache = (HashMap<IElectricityNetwork, Float>) networkEnergyCache.clone();

		if (Math.abs(worldObj.getWorldTime() - lastCheckTime) >= 40)
		{
			lastCheckTime = worldObj.getWorldTime();
			networkEnergyCache.clear();
		}

		return returnCache;
	}

	@ForgeSubscribe
	public void event(ElectricityProductionEvent evt)
	{
		IElectricityNetwork network = evt.network;

		if (evt.electricityPack.getWatts() != 0)
		{
			networkEnergyCache.put(network, evt.electricityPack.getWatts());
		}
	}
}
