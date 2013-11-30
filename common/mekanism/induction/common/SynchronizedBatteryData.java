package mekanism.induction.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.common.Object3D;
import mekanism.common.util.ListUtils;
import mekanism.induction.common.tileentity.TileEntityBattery;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;

public class SynchronizedBatteryData
{
	public Set<Object3D> locations = new HashSet<Object3D>();

	public List<ItemStack> inventory = new ArrayList<ItemStack>();

	/**
	 * Slot 0: Cell input slot Slot 1: Battery charge slot Slot 2: Battery discharge slot
	 */
	public ItemStack[] visibleInventory = new ItemStack[3];

	public ItemStack tempStack;

	public boolean isMultiblock;

	public boolean didTick;

	public boolean wroteInventory;

	public int getMaxCells()
	{
		return getVolume() * BatteryManager.CELLS_PER_BATTERY;
	}

	public int getVolume()
	{
		return locations.size();
	}
	
	public boolean addCell(ItemStack cell)
	{
		if(inventory.size() < getMaxCells())
		{
			inventory.add(cell);
			sortInventory();
			return true;
		}

		return false;
	}

	public void sortInventory()
	{
		Object[] array = ListUtils.copy(inventory).toArray();

		ItemStack[] toSort = new ItemStack[array.length];

		for(int i = 0; i < array.length; i++)
		{
			toSort[i] = (ItemStack) array[i];
		}

		boolean cont = true;
		ItemStack temp;

		while(cont)
		{
			cont = false;

			for(int i = 0; i < toSort.length - 1; i++)
			{
				if(((IItemElectric) toSort[i].getItem()).getElectricityStored(toSort[i]) < ((IItemElectric) toSort[i + 1].getItem()).getElectricityStored(toSort[i + 1]))
				{
					temp = toSort[i];
					toSort[i] = toSort[i + 1];
					toSort[i + 1] = temp;
					cont = true;
				}
			}
		}

		inventory = new ArrayList<ItemStack>();

		for(ItemStack itemStack : toSort)
		{
			inventory.add(itemStack);
		}
	}

	public boolean hasVisibleInventory()
	{
		for(ItemStack itemStack : visibleInventory)
		{
			if(itemStack != null)
			{
				return true;
			}
		}

		return false;
	}

	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity, List<ItemStack> inventory)
	{
		SynchronizedBatteryData structure = getBase(tileEntity);
		structure.inventory = inventory;

		return structure;
	}

	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity)
	{
		SynchronizedBatteryData structure = new SynchronizedBatteryData();
		structure.locations.add(new Object3D(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));

		return structure;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * locations.hashCode();
		return code;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof SynchronizedBatteryData))
		{
			return false;
		}

		SynchronizedBatteryData data = (SynchronizedBatteryData) obj;

		if(!data.locations.equals(locations))
		{
			return false;
		}

		return true;
	}
}
