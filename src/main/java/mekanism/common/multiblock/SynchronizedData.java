package mekanism.common.multiblock;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraft.item.ItemStack;

public abstract class SynchronizedData<T>
{
	public Set<Coord4D> locations = new HashSet<Coord4D>();

	public int volLength;

	public int volWidth;

	public int volHeight;

	public int volume;
	
	public int inventoryID;
	
	public boolean didTick;

	public boolean hasRenderer;

	public Coord4D renderLocation;
	
	public ItemStack[] getInventory()
	{
		return null;
	}
	
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
		if(obj == null || obj.getClass() != getClass())
		{
			return false;
		}

		SynchronizedData<T> data = (SynchronizedData<T>)obj;

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
}
