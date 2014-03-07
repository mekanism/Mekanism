package mekanism.common.transporter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.EnumColor;
import mekanism.api.Coord4D;
import mekanism.common.IInvConfiguration;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public class TransporterManager
{
	public static Set<TransporterStack> flowingStacks = new HashSet<TransporterStack>();
	
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
	
	public static ItemStack[] copyInvFromSide(IInventory inv, int side)
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
		}
		
		return ret;
	}
	
	public static void testInsert(IInventory inv, ItemStack[] testInv, int side, TransporterStack stack)
	{
		ItemStack toInsert = stack.itemStack.copy();
		
    	if(stack.pathType != Path.HOME && inv instanceof IInvConfiguration)
    	{
    		IInvConfiguration config = (IInvConfiguration)inv;
    		int tileSide = config.getOrientation();
    		EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());
    		
    		if(config.getEjector().hasStrictInput() && configColor != null && configColor != stack.color)
    		{
    			return;
    		}
    	}
    	
    	if(inv instanceof IDeepStorageUnit)
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
				
				ItemStack inSlot = testInv[i];
	
				if(inSlot == null)
				{
					testInv[i] = toInsert;
					return;
				} 
				else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize()) 
				{
					if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize()) 
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;
	
						testInv[i] = toSet;
						return;
					} 
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();
	
						ItemStack toSet = toInsert.copy();
						toSet.stackSize = inSlot.getMaxStackSize();
	
						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;
	
						testInv[i] = toSet;
						
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
				
				for(int get = 0; get <= slots.length - 1; get++) 
				{
					int slotID = slots[get];
	
					if(stack.pathType != Path.HOME)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) && !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
						{
							continue;
						}
					}
					
					ItemStack inSlot = testInv[slotID];
	
					if(inSlot == null) 
					{
						testInv[slotID] = toInsert;
						return;
					} 
					else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize()) 
						{
							ItemStack toSet = toInsert.copy();
							toSet.stackSize += inSlot.stackSize;
	
							testInv[slotID] = toSet;
							return;
						} 
						else {
							int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();
	
							ItemStack toSet = toInsert.copy();
							toSet.stackSize = inSlot.getMaxStackSize();
	
							ItemStack remains = toInsert.copy();
							remains.stackSize = rejects;
	
							testInv[slotID] = toSet;
							
							toInsert = remains;
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
    	
    	if(tileEntity instanceof IInvConfiguration)
    	{
    		IInvConfiguration config = (IInvConfiguration)tileEntity;
    		int tileSide = config.getOrientation();
    		EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());
    		
    		if(config.getEjector().hasStrictInput() && configColor != null && configColor != color)
    		{
    			return itemStack;
    		}
    	}
    	
    	IInventory inventory = (IInventory)tileEntity;
    	ItemStack[] testInv = copyInvFromSide(inventory, side);
    	
    	if(testInv == null)
    	{
    		return itemStack;
    	}
    	
    	List<TransporterStack> insertQueue = getStacksToDest(Coord4D.get(tileEntity));
    	
    	for(TransporterStack tStack : insertQueue)
    	{
    		testInsert(inventory, testInv, side, tStack);
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
				
				ItemStack inSlot = testInv[i];

				if(inSlot == null || toInsert == null)
				{
					return null;
				} 
				else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize()) 
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
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				/*if(sidedInventory instanceof TileEntityBin && ForgeDirection.getOrientation(side).getOpposite().ordinal() == 0)
				{
					slots = sidedInventory.getAccessibleSlotsFromSide(1);
				}*/
				
				for(int get = 0; get <= slots.length - 1; get++) 
				{
					int slotID = slots[get];
	
					if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
					{
						continue;
					}
					
					ItemStack inSlot = testInv[slotID];

					if(inSlot == null) 
					{
						return null;
					} 
					else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize())
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
    	
    	return toInsert;
	}
}
