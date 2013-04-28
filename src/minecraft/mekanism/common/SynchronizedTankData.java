package mekanism.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import mekanism.api.Object3D;

public class SynchronizedTankData 
{
	public Set<Object3D> locations = new HashSet<Object3D>();
	
	public int volLength;
	
	public int volWidth;
	
	public int volHeight;
	
	public int volume;
	
	public LiquidStack liquidStored;
	
	public ItemStack[] inventory = new ItemStack[2];
	
	public boolean didTick;
	
	public boolean hasRenderer;
	
	public Object3D renderLocation;
	
	public Set<ValveData> valves = new HashSet<ValveData>();
	
	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * locations.hashCode();
		code = 31 * volLength;
		code = 31 * volWidth;
		code = 31 * volHeight;
		code = 31 * volume;
		return code;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof SynchronizedTankData))
		{
			return false;
		}
		
		SynchronizedTankData data = (SynchronizedTankData)obj;
		
		if(!data.locations.equals(locations))
		{
			return false;
		}
		
		if(data.volLength != volLength || data.volWidth != volWidth || data.volHeight != volHeight)
		{
			return false;
		}
		
		if(data.volume != volume)
		{
			return false;
		}
		
		return true;
	}
	
	public static class ValveData
	{
		public ForgeDirection side;
		public Object3D location;
		public boolean serverLiquid;
		
		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + side.ordinal();
			code = 31 * code + location.hashCode();
			return code;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof ValveData && ((ValveData)obj).side == side && ((ValveData)obj).location.equals(location);
		}
	}
}
