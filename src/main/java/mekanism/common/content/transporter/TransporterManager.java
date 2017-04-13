package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public class TransporterManager
{
	public static Map<Coord4D, Set<TransporterStack>> flowingStacks = new HashMap<Coord4D, Set<TransporterStack>>();
	
	public static void reset()
	{
		flowingStacks.clear();
	}

	public static void add(TransporterStack stack)
	{
		Set<TransporterStack> set = new HashSet<TransporterStack>();
		set.add(stack);
		
		if(flowingStacks.get(stack.getDest()) == null)
		{
			flowingStacks.put(stack.getDest(), set);
		}
		else {
			flowingStacks.get(stack.getDest()).addAll(set);
		}
	}

	public static void remove(TransporterStack stack)
	{
		if(stack.hasPath() && stack.pathType != Path.NONE)
		{
			flowingStacks.get(stack.getDest()).remove(stack);
		}
	}

	public static List<TransporterStack> getStacksToDest(Coord4D dest)
	{
		List<TransporterStack> ret = new ArrayList<TransporterStack>();

		if(flowingStacks.containsKey(dest))
		{
			for(TransporterStack stack : flowingStacks.get(dest))
			{
				if(stack != null && stack.pathType != Path.NONE && stack.hasPath())
				{
					if(stack.getDest().equals(dest))
					{
						ret.add(stack);
					}
				}
			}
		}

		return ret;
	}
	
	public static InventoryCopy copyInv(IItemHandler handler)
	{
		NonNullList<ItemStack> ret = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
		
		for(int i = 0; i < handler.getSlots(); i++)
		{
			ret.set(i, handler.getStackInSlot(i));
		}
		
		return new InventoryCopy(ret);
	}

	public static InventoryCopy copyInvFromSide(IInventory inv, EnumFacing side)
	{
		inv = InventoryUtils.checkChestInv(inv);
		
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		if(!(inv instanceof ISidedInventory))
		{
			for(int i = 0; i <= inv.getSizeInventory() - 1; i++)
			{
				ret.set(i, !inv.getStackInSlot(i).isEmpty() ? inv.getStackInSlot(i).copy() : ItemStack.EMPTY);
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inv;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots == null || slots.length == 0)
			{
				return null;
			}

			for(int get = 0; get <= slots.length - 1; get++)
			{
				int slotID = slots[get];

				ret.set(slotID, !sidedInventory.getStackInSlot(slotID).isEmpty() ? sidedInventory.getStackInSlot(slotID).copy() : ItemStack.EMPTY);
			}
			
			if(inv instanceof TileEntityBin)
			{
				return new InventoryCopy(ret, ((TileEntityBin)inv).getItemCount());
			}
			else {
				return new InventoryCopy(ret);
			}
		}

		return new InventoryCopy(ret);
	}

	public static void testInsert(IInventory inv, InventoryCopy copy, EnumFacing side, TransporterStack stack)
	{
		ItemStack toInsert = stack.itemStack.copy();

		if(stack.pathType != Path.HOME && inv instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)inv;
			EnumFacing tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

			if(config.getEjector().hasStrictInput() && configColor != null && configColor != stack.color)
			{
				return;
			}
		}
		
		if(Loader.isModLoaded("MinefactoryReloaded") && inv instanceof IDeepStorageUnit && !(inv instanceof TileEntityBin))
		{
			return;
		}

		if(!(inv instanceof ISidedInventory))
		{
			for(int i = 0; i <= inv.getSizeInventory() - 1; i++)
			{
				if(stack.pathType != Path.HOME)
				{
					if(!inv.isItemValidForSlot(i, toInsert))
					{
						continue;
					}
				}

				ItemStack inSlot = copy.inventory.get(i);

				if(inSlot.isEmpty())
				{
					if(toInsert.getCount() <= inv.getInventoryStackLimit())
					{
						copy.inventory.set(i, toInsert);
						return;
					}
					else {
						int rejects = toInsert.getCount() - inv.getInventoryStackLimit();
						
						ItemStack toSet = toInsert.copy();
						toSet.setCount(inv.getInventoryStackLimit());

						ItemStack remains = toInsert.copy();
						remains.setCount(rejects);

						copy.inventory.set(i, toSet);

						toInsert = remains;
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit());
					
					if(inSlot.getCount() + toInsert.getCount() <= max)
					{
						ItemStack toSet = toInsert.copy();
						toSet.grow(inSlot.getCount());

						copy.inventory.set(i, toSet);
						return;
					}
					else {
						int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

						ItemStack toSet = toInsert.copy();
						toSet.setCount(max);

						ItemStack remains = toInsert.copy();
						remains.setCount(rejects);

						copy.inventory.set(i, toSet);

						toInsert = remains;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inv;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(stack.pathType != Path.HOME && sidedInventory instanceof TileEntityBin && side.getOpposite() == EnumFacing.DOWN)
				{
					slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
				}

				if(inv instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, side.getOpposite()))
					{
						return;
					}
					
					int amountRemaining = ((TileEntityBin)inv).getMaxStoredCount()-copy.binAmount;
					copy.binAmount += Math.min(amountRemaining, toInsert.getCount());
					
					return;
				}
				else {
					for(int get = 0; get <= slots.length - 1; get++)
					{
						int slotID = slots[get];
	
						if(stack.pathType != Path.HOME)
						{
							if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, side.getOpposite()))
							{
								continue;
							}
						}
	
						ItemStack inSlot = copy.inventory.get(slotID);
	
						if(inSlot.isEmpty())
						{
							if(toInsert.getCount() <= inv.getInventoryStackLimit())
							{
								copy.inventory.set(slotID, toInsert);
								return;
							}
							else {
								int rejects = toInsert.getCount() - inv.getInventoryStackLimit();
								
								ItemStack toSet = toInsert.copy();
								toSet.setCount(inv.getInventoryStackLimit());

								ItemStack remains = toInsert.copy();
								remains.setCount(rejects);

								copy.inventory.set(slotID, toSet);

								toInsert = remains;
							}
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit()))
						{
							int max = Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit());
							
							if(inSlot.getCount() + toInsert.getCount() <= max)
							{
								ItemStack toSet = toInsert.copy();
								toSet.grow(inSlot.getCount());
	
								copy.inventory.set(slotID, toSet);
								return;
							}
							else {
								int rejects = (inSlot.getCount() + toInsert.getCount()) - max;
	
								ItemStack toSet = toInsert.copy();
								toSet.setCount(max);
	
								ItemStack remains = toInsert.copy();
								remains.setCount(rejects);
	
								copy.inventory.set(slotID, toSet);
	
								toInsert = remains;
							}
						}
					}
				}
			}
		}
	}

	public static boolean didEmit(ItemStack stack, ItemStack returned)
	{
		return returned.isEmpty() || returned.getCount() < stack.getCount();
	}

	public static ItemStack getToUse(ItemStack stack, ItemStack returned)
	{
		if(returned.isEmpty() || returned.getCount() == 0)
		{
			return stack;
		}

		return MekanismUtils.size(stack, stack.getCount()-returned.getCount());
	}

	/**
	 * @return rejects
	 */
	public static ItemStack getPredictedInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, EnumFacing side)
	{
		if(!(tileEntity instanceof IInventory))
		{
			return itemStack;
		}

		if(tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;
			EnumFacing tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

			if(config.getEjector().hasStrictInput() && configColor != null && configColor != color)
			{
				return itemStack;
			}
		}

		IInventory inventory = (IInventory)tileEntity;
		InventoryCopy copy = copyInvFromSide(inventory, side);

		if(copy == null)
		{
			return itemStack;
		}

		List<TransporterStack> insertQueue = getStacksToDest(Coord4D.get(tileEntity));

		for(TransporterStack tStack : insertQueue)
		{
			testInsert(inventory, copy, side, tStack);
		}

		ItemStack toInsert = itemStack.copy();

		if(!(inventory instanceof ISidedInventory))
		{
			inventory = InventoryUtils.checkChestInv(inventory);

			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!inventory.isItemValidForSlot(i, toInsert))
				{
					continue;
				}

				ItemStack inSlot = copy.inventory.get(i);

				if(toInsert.isEmpty())
				{
					return ItemStack.EMPTY;
				}
				else if(inSlot.isEmpty())
				{
					if(toInsert.getCount() <= inventory.getInventoryStackLimit())
					{
						return ItemStack.EMPTY;
					}
					else {
						int rejects = toInsert.getCount() - inventory.getInventoryStackLimit();
						
						if(rejects < toInsert.getCount())
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
					
					if(inSlot.getCount() + toInsert.getCount() <= max)
					{
						return ItemStack.EMPTY;
					}
					else {
						int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

						if(rejects < toInsert.getCount())
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(inventory instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, side.getOpposite()))
					{
						return toInsert;
					}
					
					int amountRemaining = ((TileEntityBin)inventory).getMaxStoredCount()-copy.binAmount;
					
					if(toInsert.getCount() <= amountRemaining)
					{
						return ItemStack.EMPTY;
					}
					else {
						return StackUtils.size(toInsert, toInsert.getCount()-amountRemaining);
					}
				}
				else {
					for(int get = 0; get <= slots.length - 1; get++)
					{
						int slotID = slots[get];
	
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, side.getOpposite()))
						{
							continue;
						}
	
						ItemStack inSlot = copy.inventory.get(slotID);
						
						if(toInsert.isEmpty())
						{
							return ItemStack.EMPTY;
						}
						else if(inSlot.isEmpty())
						{
							if(toInsert.getCount() <= inventory.getInventoryStackLimit())
							{
								return ItemStack.EMPTY;
							}
							else {
								int rejects = toInsert.getCount() - inventory.getInventoryStackLimit();
								
								if(rejects < toInsert.getCount())
								{
									toInsert = StackUtils.size(toInsert, rejects);
								}
							}
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
						{
							int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
							
							if(inSlot.getCount() + toInsert.getCount() <= max)
							{
								return ItemStack.EMPTY;
							}
							else {
								int rejects = (inSlot.getCount() + toInsert.getCount()) - max;
	
								if(rejects < toInsert.getCount())
								{
									toInsert = StackUtils.size(toInsert, rejects);
								}
							}
						}
					}
				}
			}
		}

		return toInsert;
	}
	
	public static class InventoryCopy
	{
		public NonNullList<ItemStack> inventory;
		
		public int binAmount;
		
		public InventoryCopy(NonNullList<ItemStack> inv)
		{
			inventory = inv;
		}
		
		public InventoryCopy(NonNullList<ItemStack> inv, int amount)
		{
			this(inv);
			binAmount = amount;
		}
	}
}
