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
		ItemStack[] ret = new ItemStack[handler.getSlots()];
		
		for(int i = 0; i < handler.getSlots(); i++)
		{
			ret[i] = handler.getStackInSlot(i);
		}
		
		return new InventoryCopy(ret);
	}

	public static InventoryCopy copyInvFromSide(IInventory inv, EnumFacing side)
	{
		ItemStack[] ret = new ItemStack[inv.getSizeInventory()];

		if(!(inv instanceof ISidedInventory))
		{
			for(int i = 0; i <= inv.getSizeInventory() - 1; i++)
			{
				ret[i] = inv.getStackInSlot(i) != null ? inv.getStackInSlot(i).copy() : null;
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

				ret[slotID] = sidedInventory.getStackInSlot(slotID) != null ? sidedInventory.getStackInSlot(slotID).copy() : null;
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

	public static void testInsert(TileEntity tile, InventoryCopy copy, EnumFacing side, TransporterStack stack)
	{
		ItemStack toInsert = stack.itemStack.copy();

		if(stack.pathType != Path.HOME && tile instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tile;
			EnumFacing tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

			if(config.getEjector().hasStrictInput() && configColor != null && configColor != stack.color)
			{
				return;
			}
		}
		
		if(Loader.isModLoaded("MinefactoryReloaded") && tile instanceof IDeepStorageUnit && !(tile instanceof TileEntityBin))
		{
			return;
		}

		if(tile instanceof ISidedInventory)
		{
			ISidedInventory sidedInventory = (ISidedInventory)tile;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(stack.pathType != Path.HOME && sidedInventory instanceof TileEntityBin && side.getOpposite() == EnumFacing.DOWN)
				{
					slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
				}

				if(tile instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, side.getOpposite()))
					{
						return;
					}
					
					int amountRemaining = ((TileEntityBin)sidedInventory).getMaxStoredCount()-copy.binAmount;
					copy.binAmount += Math.min(amountRemaining, toInsert.stackSize);
					
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
	
						ItemStack inSlot = copy.inventory[slotID];
	
						if(inSlot == null)
						{
							if(toInsert.stackSize <= sidedInventory.getInventoryStackLimit())
							{
								copy.inventory[slotID] = toInsert;
								return;
							}
							else {
								int rejects = toInsert.stackSize - sidedInventory.getInventoryStackLimit();
								
								ItemStack toSet = toInsert.copy();
								toSet.stackSize = sidedInventory.getInventoryStackLimit();

								ItemStack remains = toInsert.copy();
								remains.stackSize = rejects;

								copy.inventory[slotID] = toSet;

								toInsert = remains;
							}
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit()))
						{
							int max = Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit());
							
							if(inSlot.stackSize + toInsert.stackSize <= max)
							{
								ItemStack toSet = toInsert.copy();
								toSet.stackSize += inSlot.stackSize;
	
								copy.inventory[slotID] = toSet;
								return;
							}
							else {
								int rejects = (inSlot.stackSize + toInsert.stackSize) - max;
	
								ItemStack toSet = toInsert.copy();
								toSet.stackSize = max;
	
								ItemStack remains = toInsert.copy();
								remains.stackSize = rejects;
	
								copy.inventory[slotID] = toSet;
	
								toInsert = remains;
							}
						}
					}
				}
			}
		}
		else if(tile instanceof IInventory)
		{
			IInventory inv = InventoryUtils.checkChestInv((IInventory)tile);
			
			for(int i = 0; i <= inv.getSizeInventory() - 1; i++)
			{
				if(stack.pathType != Path.HOME)
				{
					if(!inv.isItemValidForSlot(i, toInsert))
					{
						continue;
					}
				}

				ItemStack inSlot = copy.inventory[i];

				if(inSlot == null)
				{
					if(toInsert.stackSize <= inv.getInventoryStackLimit())
					{
						copy.inventory[i] = toInsert;
						return;
					}
					else {
						int rejects = toInsert.stackSize - inv.getInventoryStackLimit();
						
						ItemStack toSet = toInsert.copy();
						toSet.stackSize = inv.getInventoryStackLimit();

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						copy.inventory[i] = toSet;

						toInsert = remains;
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inv.getInventoryStackLimit());
					
					if(inSlot.stackSize + toInsert.stackSize <= max)
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;

						copy.inventory[i] = toSet;
						return;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - max;

						ItemStack toSet = toInsert.copy();
						toSet.stackSize = max;

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						copy.inventory[i] = toSet;

						toInsert = remains;
					}
				}
			}
		}
		else if(InventoryUtils.isItemHandler(tile, side.getOpposite()))
		{
			IItemHandler inv = InventoryUtils.getItemHandler(tile, side.getOpposite());
			
			for(int i = 0; i <= inv.getSlots() - 1; i++)
			{
				if(stack.pathType != Path.HOME)
				{
					ItemStack rejectStack = inv.insertItem(i, toInsert, true);
					
					if(!TransporterManager.didEmit(toInsert, rejectStack))
					{
						continue;
					}
				}

				ItemStack inSlot = copy.inventory[i];

				if(inSlot == null)
				{
					if(toInsert.stackSize <= toInsert.getMaxStackSize())
					{
						copy.inventory[i] = toInsert;
						return;
					}
					else {
						int rejects = toInsert.stackSize - toInsert.getMaxStackSize();
						
						ItemStack toSet = toInsert.copy();
						toSet.stackSize = toInsert.getMaxStackSize();

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						copy.inventory[i] = toSet;

						toInsert = remains;
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize())
				{
					int max = inSlot.getMaxStackSize();
					
					if(inSlot.stackSize + toInsert.stackSize <= max)
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;

						copy.inventory[i] = toSet;
						return;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - max;

						ItemStack toSet = toInsert.copy();
						toSet.stackSize = max;

						ItemStack remains = toInsert.copy();
						remains.stackSize  = rejects;

						copy.inventory[i] = toSet;

						toInsert = remains;
					}
				}
			}
		}
	}

	public static boolean didEmit(ItemStack stack, ItemStack returned)
	{
		return returned == null || returned.stackSize < stack.stackSize;
	}

	public static ItemStack getToUse(ItemStack stack, ItemStack returned)
	{
		if(returned == null || returned.stackSize == 0)
		{
			return stack;
		}

		return MekanismUtils.size(stack, stack.stackSize-returned.stackSize);
	}

	/**
	 * @return rejects
	 */
	public static ItemStack getPredictedInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, EnumFacing side)
	{
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

		InventoryCopy copy = null;
		
		if(tileEntity instanceof IInventory)
		{
			copy = copyInvFromSide(InventoryUtils.checkChestInv((IInventory)tileEntity), side);
		}
		else if(InventoryUtils.isItemHandler(tileEntity, side.getOpposite()))
		{
			copy = copyInv(InventoryUtils.getItemHandler(tileEntity, side.getOpposite()));
		}

		if(copy == null)
		{
			return itemStack;
		}

		List<TransporterStack> insertQueue = getStacksToDest(Coord4D.get(tileEntity));

		for(TransporterStack tStack : insertQueue)
		{
			testInsert(tileEntity, copy, side, tStack);
		}

		ItemStack toInsert = itemStack.copy();

		if(tileEntity instanceof ISidedInventory)
		{
			ISidedInventory sidedInventory = (ISidedInventory)tileEntity;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(tileEntity instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, side.getOpposite()))
					{
						return toInsert;
					}
					
					int amountRemaining = ((TileEntityBin)tileEntity).getMaxStoredCount()-copy.binAmount;
					
					if(toInsert.stackSize <= amountRemaining)
					{
						return null;
					}
					else {
						return StackUtils.size(toInsert, toInsert.stackSize-amountRemaining);
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
	
						ItemStack inSlot = copy.inventory[slotID];
						
						if(toInsert == null)
						{
							return null;
						}
						else if(inSlot == null)
						{
							if(toInsert.stackSize <= sidedInventory.getInventoryStackLimit())
							{
								return null;
							}
							else {
								int rejects = toInsert.stackSize - sidedInventory.getInventoryStackLimit();
								
								if(rejects < toInsert.stackSize)
								{
									toInsert = StackUtils.size(toInsert, rejects);
								}
							}
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit()))
						{
							int max = Math.min(inSlot.getMaxStackSize(), sidedInventory.getInventoryStackLimit());
							
							if(inSlot.stackSize + toInsert.stackSize <= max)
							{
								return null;
							}
							else {
								int rejects = (inSlot.stackSize + toInsert.stackSize) - max;
	
								if(rejects < toInsert.stackSize)
								{
									toInsert = StackUtils.size(toInsert, rejects);
								}
							}
						}
					}
				}
			}
		}
		else if(tileEntity instanceof IInventory)
		{
			IInventory inventory = InventoryUtils.checkChestInv((IInventory)tileEntity);
			
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!inventory.isItemValidForSlot(i, toInsert))
				{
					continue;
				}

				ItemStack inSlot = copy.inventory[i];

				if(toInsert == null)
				{
					return null;
				}
				else if(inSlot == null)
				{
					if(toInsert.stackSize <= inventory.getInventoryStackLimit())
					{
						return null;
					}
					else {
						int rejects = toInsert.stackSize - inventory.getInventoryStackLimit();
						
						if(rejects < toInsert.stackSize)
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
					
					if(inSlot.stackSize + toInsert.stackSize <= max)
					{
						return null;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - max;

						if(rejects < toInsert.stackSize)
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
			}
		}
		else if(InventoryUtils.isItemHandler(tileEntity, side.getOpposite()))
		{
			IItemHandler inventory = InventoryUtils.getItemHandler(tileEntity, side.getOpposite());
			
			for(int i = 0; i <= inventory.getSlots() - 1; i++)
			{
				ItemStack rejectStack = inventory.insertItem(i, toInsert, true);
				
				if(!TransporterManager.didEmit(toInsert, rejectStack))
				{
					continue;
				}

				if (rejectStack == null)
				{
					return null;
				}
				toInsert = rejectStack;

				/*ItemStack inSlot = copy.inventory[i];

				if(toInsert == null)
				{
					return null;
				}
				else if(inSlot == null)
				{
					if(toInsert.stackSize <= inventory.getSlotLimit(i))
					{
						return null;
					}
					else {
						int rejects = toInsert.stackSize - inventory.getSlotLimit(i);
						
						if(rejects < toInsert.stackSize)
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < Math.min(inSlot.getMaxStackSize(), inventory.getSlotLimit(i)))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inventory.getSlotLimit(i));
					
					if(inSlot.stackSize + toInsert.stackSize <= max)
					{
						return null;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - max;

						if(rejects < toInsert.stackSize)
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}*/
			}
		}

		return toInsert;
	}
	
	public static class InventoryCopy
	{
		public ItemStack[] inventory;
		
		public int binAmount;
		
		public InventoryCopy(ItemStack[] inv)
		{
			inventory = inv;
		}
		
		public InventoryCopy(ItemStack[] inv, int amount)
		{
			this(inv);
			binAmount = amount;
		}
	}
}
