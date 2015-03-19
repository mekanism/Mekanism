package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.util.StackUtils;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import cpw.mods.fml.common.Loader;

public class TransporterManager
{
	public static Set<TransporterStack> flowingStacks = new HashSet<TransporterStack>();
	
	public static void reset()
	{
		flowingStacks.clear();
	}

	public static void add(TransporterStack stack)
	{
		flowingStacks.add(stack);
	}

	public static void remove(TransporterStack stack)
	{
		flowingStacks.remove(stack);
	}

	public static List<TransporterStack> getStacksToDest(Coord4D dest)
	{
		List<TransporterStack> ret = new ArrayList<TransporterStack>();

		for(TransporterStack stack : flowingStacks)
		{
			if(stack != null && stack.pathType != Path.NONE && stack.hasPath())
			{
				if(stack.getDest().equals(dest))
				{
					ret.add(stack);
				}
			}
		}

		return ret;
	}

	public static InventoryCopy copyInvFromSide(IInventory inv, int side)
	{
		inv = InventoryUtils.checkChestInv(inv);

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
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

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

	public static void testInsert(IInventory inv, InventoryCopy copy, int side, TransporterStack stack)
	{
		ItemStack toInsert = stack.itemStack.copy();

		if(stack.pathType != Path.HOME && inv instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)inv;
			int tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());

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

				ItemStack inSlot = copy.inventory[i];

				if(inSlot == null)
				{
					copy.inventory[i] = toInsert;
					return;
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inv.getInventoryStackLimit())
				{
					if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;

						copy.inventory[i] = toSet;
						return;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

						ItemStack toSet = toInsert.copy();
						toSet.stackSize = inSlot.getMaxStackSize();

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						copy.inventory[i] = toSet;

						toInsert = remains;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inv;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				if(stack.pathType != Path.HOME && sidedInventory instanceof TileEntityBin && ForgeDirection.getOrientation(side).getOpposite().ordinal() == 0)
				{
					slots = sidedInventory.getAccessibleSlotsFromSide(1);
				}

				if(inv instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal()))
					{
						return;
					}
					
					int amountRemaining = ((TileEntityBin)inv).getMaxStoredCount()-copy.binAmount;
					copy.binAmount += Math.min(amountRemaining, toInsert.stackSize);
					
					return;
				}
				else {
					for(int get = 0; get <= slots.length - 1; get++)
					{
						int slotID = slots[get];
	
						if(stack.pathType != Path.HOME)
						{
							if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal()))
							{
								continue;
							}
						}
	
						ItemStack inSlot = copy.inventory[slotID];
	
						if(inSlot == null)
						{
							copy.inventory[slotID] = toInsert;
							return;
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inv.getInventoryStackLimit())
						{
							if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
							{
								ItemStack toSet = toInsert.copy();
								toSet.stackSize += inSlot.stackSize;
	
								copy.inventory[slotID] = toSet;
								return;
							}
							else {
								int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();
	
								ItemStack toSet = toInsert.copy();
								toSet.stackSize = inSlot.getMaxStackSize();
	
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
	public static ItemStack getPredictedInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, int side)
	{
		if(!(tileEntity instanceof IInventory))
		{
			return itemStack;
		}

		if(tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;
			int tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());

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

				ItemStack inSlot = copy.inventory[i];

				if(inSlot == null || toInsert == null)
				{
					return null;
				}
				else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
				{
					if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
					{
						return null;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

						if(rejects < toInsert.stackSize)
						{
							toInsert = StackUtils.size(toInsert, rejects);
						}
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				if(inventory instanceof TileEntityBin)
				{
					int slot = slots[0];
					
					if(!sidedInventory.isItemValidForSlot(slot, toInsert) || !sidedInventory.canInsertItem(slot, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal()))
					{
						return toInsert;
					}
					
					int amountRemaining = ((TileEntityBin)inventory).getMaxStoredCount()-copy.binAmount;
					
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
	
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal()))
						{
							continue;
						}
	
						ItemStack inSlot = copy.inventory[slotID];
	
						if(inSlot == null)
						{
							return null;
						}
						else if(InventoryUtils.areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
						{
							if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
							{
								return null;
							}
							else {
								int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();
	
								if(rejects < toInsert.stackSize)
								{
									toInsert = MekanismUtils.size(toInsert, rejects);
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
