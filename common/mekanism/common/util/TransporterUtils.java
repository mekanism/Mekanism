package mekanism.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.SlotInfo;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterUtils 
{
	public static ArrayList<EnumColor> colors = buildColors();
	
	public static ArrayList<EnumColor> buildColors()
	{
		ArrayList<EnumColor> ret = new ArrayList<EnumColor>();
		
		ret.add(EnumColor.DARK_BLUE);
		ret.add(EnumColor.DARK_GREEN);
		ret.add(EnumColor.DARK_AQUA);
		ret.add(EnumColor.DARK_RED);
		ret.add(EnumColor.PURPLE);
		ret.add(EnumColor.INDIGO);
		ret.add(EnumColor.BRIGHT_GREEN);
		ret.add(EnumColor.AQUA);
		ret.add(EnumColor.RED);
		ret.add(EnumColor.PINK);
		ret.add(EnumColor.YELLOW);
		
		return ret;
	}

    /**
     * Gets all the transporters around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedTransporters(TileEntity tileEntity)
    {
    	TileEntity[] transporters = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity transporter = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(TransmissionType.checkTransmissionType(transporter, TransmissionType.ITEM))
			{
				transporters[orientation.ordinal()] = transporter;
			}
    	}
    	
    	return transporters;
    }

    /**
     * Gets all the adjacent connections to a TileEntity.
     * @param tileEntity - center TileEntity
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntity tileEntity)
    {
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		TileEntity[] connectedTransporters = getConnectedTransporters(tileEntity);
		IInventory[] connectedInventories = getConnectedInventories(tileEntity);
		
		for(IInventory inventory : connectedInventories)
		{
			if(inventory != null)
			{
				int side = Arrays.asList(connectedInventories).indexOf(inventory);
				ForgeDirection forgeSide = ForgeDirection.getOrientation(side).getOpposite();
				
				if(inventory.getSizeInventory() > 0)
				{
					if(inventory instanceof ISidedInventory)
					{
						ISidedInventory sidedInventory = (ISidedInventory)inventory;
						
						if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()) != null)
						{
							if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()).length > 0)
							{
								connectable[side] = true;
							}
						}
					}
					else {
						connectable[side] = true;
					}
				}
			}
		}
		
		for(TileEntity tile : connectedTransporters)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedTransporters).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		return connectable;
    }
    
    /**
     * Gets all the inventories around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of IInventories
     */
    public static IInventory[] getConnectedInventories(TileEntity tileEntity)
    {
    	IInventory[] inventories = new IInventory[] {null, null, null, null, null, null};

    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity inventory = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(inventory instanceof IInventory && !(inventory instanceof ITransmitter))
			{
				inventories[orientation.ordinal()] = (IInventory)inventory;
			}
    	}
    	
    	return inventories;
    }
    
    public static boolean insert(TileEntity outputter, TileEntityLogisticalTransporter tileEntity, ItemStack itemStack, EnumColor color)
    {
    	return tileEntity.insert(Object3D.get(outputter), itemStack, color);
    }
    
    public static boolean canInsert(TileEntity tileEntity, ItemStack itemStack, int side)
    {
    	if(!(tileEntity instanceof IInventory))
    	{
    		return false;
    	}
    	
    	IInventory inventory = (IInventory)tileEntity;
    	
    	if(!(inventory instanceof ISidedInventory))
		{
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(inventory.isItemValidForSlot(i, itemStack)) 
				{
					ItemStack inSlot = inventory.getStackInSlot(i);

					if(inSlot == null)
					{
						return true;
					} 
					else if(inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize()) 
					{
						if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize()) 
						{
							return true;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							if(rejects < itemStack.stackSize)
							{
								return true;
							}
						}
					}
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			for(int get = 0; get <= slots.length - 1; get++) 
			{
				int slotID = slots[get];

				if(sidedInventory.isItemValidForSlot(slotID, itemStack) && sidedInventory.canInsertItem(slotID, itemStack, side)) 
				{
					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot == null) 
					{
						return true;
					} 
					else if(inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize()) 
						{
							return true;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();
							
							if(rejects < itemStack.stackSize)
							{
								return true;
							}
						}
					}
				}
			}
		}
    	
    	return false;
    }
    
	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side) 
	{
		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(inventory.isItemValidForSlot(i, itemStack)) 
				{
					ItemStack inSlot = inventory.getStackInSlot(i);

					if(inSlot == null)
					{
						inventory.setInventorySlotContents(i, itemStack);
						return null;
					} 
					else if(inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize()) 
					{
						if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize()) 
						{
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(i, toSet);
							return null;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(i, toSet);
							return remains;
						}
					}
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			for(int get = 0; get <= slots.length - 1; get++) 
			{
				int slotID = slots[get];

				if(sidedInventory.isItemValidForSlot(slotID, itemStack) && sidedInventory.canInsertItem(slotID, itemStack, side)) 
				{
					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot == null) 
					{
						inventory.setInventorySlotContents(slotID, itemStack);
						return null;
					} 
					else if(inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize()) 
						{
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(slotID, toSet);
							return null;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(slotID, toSet);
							return remains;
						}
					}
				}
			}
		}

		return itemStack;
	}

	public static SlotInfo takeItem(IInventory inventory, int side) 
	{
		if(!(inventory instanceof ISidedInventory)) 
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--) 
			{
				if(inventory.getStackInSlot(i) != null) 
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					inventory.setInventorySlotContents(i, null);

					return new SlotInfo(toSend, i);
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			if(slots != null) 
			{
				for(int get = slots.length - 1; get >= 0; get--) 
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null) 
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID);

						if(sidedInventory.canExtractItem(slotID, toSend, side)) 
						{
							sidedInventory.setInventorySlotContents(slotID, null);

							return new SlotInfo(toSend, slotID);
						}
					}
				}
			}
		}

		return null;
	}
	
    public static void incrementColor(TileEntityLogisticalTransporter tileEntity)
    {
    	if(tileEntity.color == null)
    	{
    		tileEntity.color = colors.get(0);
    		return;
    	}
    	else if(colors.indexOf(tileEntity.color) == colors.size()-1)
    	{
    		tileEntity.color = null;
    		return;
    	}
    	
    	int index = colors.indexOf(tileEntity.color);
    	tileEntity.color = colors.get(index+1);
    }
}