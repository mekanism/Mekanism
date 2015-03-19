package mekanism.common.content.tank;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTankData extends SynchronizedData<SynchronizedTankData>
{
	public FluidStack fluidStored;
	
	public ContainerEditMode editMode = ContainerEditMode.BOTH;

	public ItemStack[] inventory = new ItemStack[2];

	public Set<ValveData> valves = new HashSet<ValveData>();
	
	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
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
