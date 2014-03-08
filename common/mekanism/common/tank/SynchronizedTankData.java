package mekanism.common.tank;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTankData
{
	public Set<Coord4D> locations = new HashSet<Coord4D>();

	public int volLength;

	public int volWidth;

	public int volHeight;

	public int volume;

	public FluidStack fluidStored;

	public ItemStack[] inventory = new ItemStack[2];

	public boolean didTick;

	public boolean hasRenderer;

	public Coord4D renderLocation;

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
		public Coord4D location;
		public boolean serverFluid;

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
