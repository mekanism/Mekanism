package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.gas.GasTank;
import mekanism.common.base.ITankManager;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class SideData
{
	/** The color of this SideData */
	public EnumColor color;
	
	/** The name of this SideData */
	public String name;

	/** Int[] of available side slots, can be used for items, gases, or items */
	public int[] availableSlots;
	
	/** EnergyState representing this SideData */
	public EnergyState energyState;

	public SideData(String n, EnumColor colour, int[] slots)
	{
		name = n;
		color = colour;
		availableSlots = slots;
	}
	
	public SideData(String n, EnumColor colour, EnergyState state)
	{
		name = n;
		color = colour;
		energyState = state;
	}
	
	public String localize()
	{
		return MekanismUtils.localize("sideData." + name);
	}
	
	public boolean hasSlot(int... slots)
	{
		for(int i : availableSlots)
		{
			for(int slot : slots)
			{
				if(i == slot)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public FluidTankInfo[] getFluidTankInfo(ITankManager manager)
	{
		Object[] tanks = manager.getTanks();
		List<FluidTankInfo> infos = new ArrayList<FluidTankInfo>();
		
		for(int slot : availableSlots)
		{
			if(slot <= tanks.length-1 && tanks[slot] instanceof IFluidTank)
			{
				infos.add(((IFluidTank)tanks[slot]).getInfo());
			}
		}
		
		return infos.toArray(new FluidTankInfo[] {});
	}
	
	public GasTank getGasTank(ITankManager manager)
	{
		Object[] tanks = manager.getTanks();
		
		if(tanks.length < 1 || !(tanks[0] instanceof GasTank))
		{
			return null;
		}
		
		return (GasTank)tanks[0];
	}
	
	public static enum EnergyState
	{
		INPUT,
		OUTPUT,
		OFF;
	}
}
