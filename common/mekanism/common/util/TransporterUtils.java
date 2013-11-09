package mekanism.common.util;

import java.util.ArrayList;
import java.util.Arrays;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.InvStack;
import mekanism.common.transporter.TransporterStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.Type;

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
    public static TileEntity[] getConnectedTransporters(TileEntityLogisticalTransporter tileEntity)
    {
    	TileEntity[] transporters = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity tile = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(tile instanceof TileEntityLogisticalTransporter)
			{
				TileEntityLogisticalTransporter transporter = (TileEntityLogisticalTransporter)tile;
				
				if(transporter.color == null || tileEntity.color == null || transporter.color == tileEntity.color)
				{
					transporters[orientation.ordinal()] = transporter;
				}
			}
    	}
    	
    	return transporters;
    }

    /**
     * Gets all the adjacent connections to a TileEntity.
     * @param tileEntity - center TileEntity
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntityLogisticalTransporter tileEntity)
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
				
				//Immature BuildCraft inv check
				if(MekanismUtils.useBuildcraft() && inventory instanceof IPowerReceptor)
				{
					if(((IPowerReceptor)inventory).getPowerReceiver(forgeSide).getType() == Type.MACHINE)
					{
						connectable[side] = true;
						continue;
					}
				}
				
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
    public static IInventory[] getConnectedInventories(TileEntityLogisticalTransporter tileEntity)
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
    	return tileEntity.insert(Object3D.get(outputter), itemStack.copy(), color);
    }
    
    public static boolean insertRR(TileEntityLogisticalSorter outputter, TileEntityLogisticalTransporter tileEntity, ItemStack itemStack, EnumColor color)
    {
    	return tileEntity.insertRR(outputter, itemStack.copy(), color);
    }
    
    public static boolean canInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, int side, boolean force)
    {
    	if(!(tileEntity instanceof IInventory))
    	{
    		return false;
    	}
    	
    	if(force && tileEntity instanceof TileEntityLogisticalSorter)
    	{
    		return ((TileEntityLogisticalSorter)tileEntity).canSendHome(itemStack);
    	}
    	
    	if(!force && tileEntity instanceof IConfigurable)
    	{
    		IConfigurable config = (IConfigurable)tileEntity;
    		int tileSide = config.getOrientation();
    		EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());
    		
    		if(config.getEjector().hasStrictInput() && configColor != null && configColor != color)
    		{
    			return false;
    		}
    	}
    	
    	IInventory inventory = (IInventory)tileEntity;
    	
    	if(!(inventory instanceof ISidedInventory))
		{
    		inventory = checkChestInv(inventory);
    		
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!force)
				{
					if(!inventory.isItemValidForSlot(i, itemStack)) 
					{
						continue;
					}
				}
				
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
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				for(int get = 0; get <= slots.length - 1; get++) 
				{
					int slotID = slots[get];
	
					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, itemStack) || !sidedInventory.canInsertItem(slotID, itemStack, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
						{
							continue;
						}
					}
					
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
    
    public static IInventory checkChestInv(IInventory inv)
    {
    	if(inv instanceof TileEntityChest)
    	{
    		TileEntityChest main = (TileEntityChest)inv;
    		TileEntityChest adj = null;
    		
    		if(main.adjacentChestXNeg != null)
    		{
    			adj = main.adjacentChestXNeg;
    		}
    		else if(main.adjacentChestXPos != null)
    		{
    			adj = main.adjacentChestXPos;
    		}
    		else if(main.adjacentChestZNeg != null)
    		{
    			adj = main.adjacentChestZNeg;
    		}
    		else if(main.adjacentChestZPosition != null)
    		{
    			adj = main.adjacentChestZPosition;
    		}
    		
    		if(adj != null)
    		{
    			return new InventoryLargeChest("", main, adj);
    		}
    	}
    	
    	return inv;
    }
    
	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side, boolean force) 
	{
		if(force && inventory instanceof TileEntityLogisticalSorter)
		{
			return ((TileEntityLogisticalSorter)inventory).sendHome(itemStack.copy());
		}
		
		ItemStack toInsert = itemStack.copy();
		
		if(!(inventory instanceof ISidedInventory))
		{
			inventory = checkChestInv(inventory);
			
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!force)
				{
					if(!inventory.isItemValidForSlot(i, toInsert)) 
					{
						continue;
					}
				}
				
				ItemStack inSlot = inventory.getStackInSlot(i);

				if(inSlot == null)
				{
					inventory.setInventorySlotContents(i, toInsert);
					return null;
				} 
				else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize()) 
				{
					if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize()) 
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;

						inventory.setInventorySlotContents(i, toSet);
						return null;
					} 
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

						ItemStack toSet = toInsert.copy();
						toSet.stackSize = inSlot.getMaxStackSize();

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						inventory.setInventorySlotContents(i, toSet);
						
						toInsert = remains;
					}
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				for(int get = 0; get <= slots.length - 1; get++) 
				{
					int slotID = slots[get];
	
					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) && !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
						{
							continue;
						}
					}
					
					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot == null) 
					{
						inventory.setInventorySlotContents(slotID, toInsert);
						return null;
					} 
					else if(inSlot.isItemEqual(toInsert) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize()) 
						{
							ItemStack toSet = toInsert.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(slotID, toSet);
							return null;
						} 
						else {
							int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = toInsert.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = toInsert.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(slotID, toSet);
							
							toInsert = remains;
						}
					}
				}
			}
		}

		return toInsert;
	}
	
	public static InvStack takeDefinedItem(IInventory inventory, int side, ItemStack type, int min, int max)
	{
		InvStack ret = new InvStack(inventory);
		
		if(!(inventory instanceof ISidedInventory)) 
		{
			inventory = checkChestInv(inventory);
			
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--) 
			{
				if(inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).isItemEqual(type)) 
				{
					ItemStack stack = inventory.getStackInSlot(i);
					int current = ret.getStack() != null ? ret.getStack().stackSize : 0;
					
					if(current+stack.stackSize <= max)
					{
						ret.appendStack(i, stack.copy());
					}
					else {
						ItemStack copy = stack.copy();
						copy.stackSize = max-current;
						ret.appendStack(i, copy);
					}

					if(ret.getStack() != null && ret.getStack().stackSize == max)
					{
						return ret;
					}
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0) 
			{
				for(int get = slots.length - 1; get >= 0; get--) 
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null && inventory.getStackInSlot(slotID).isItemEqual(type)) 
					{
						ItemStack stack = sidedInventory.getStackInSlot(slotID);
						int current = ret.getStack() != null ? ret.getStack().stackSize : 0;
						
						if(current+stack.stackSize <= max)
						{
							ItemStack copy = stack.copy();
							
							if(sidedInventory.canExtractItem(slotID, copy, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
							{
								ret.appendStack(slotID, copy);
							}
						}
						else {
							ItemStack copy = stack.copy();
							
							if(sidedInventory.canExtractItem(slotID, copy, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
							{
								copy.stackSize = max-current;
								ret.appendStack(slotID, copy);
							}
						}

						if(ret.getStack() != null && ret.getStack().stackSize == max)
						{
							return ret;
						}
					}
				}
			}
		}
		
		if(ret != null && ret.getStack() != null && ret.getStack().stackSize >= min)
		{
			return ret;
		}

		return null;
	}

	public static InvStack takeTopItem(IInventory inventory, int side) 
	{
		if(!(inventory instanceof ISidedInventory)) 
		{
			inventory = checkChestInv(inventory);
			
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--) 
			{
				if(inventory.getStackInSlot(i) != null) 
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					return new InvStack(inventory, i, toSend);
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.getOrientation(side).getOpposite().ordinal());

			if(slots != null && slots.length != 0) 
			{
				for(int get = slots.length - 1; get >= 0; get--) 
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null) 
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID);

						if(sidedInventory.canExtractItem(slotID, toSend, ForgeDirection.getOrientation(side).getOpposite().ordinal())) 
						{
							return new InvStack(inventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}
	
	public static EnumColor increment(EnumColor color)
	{
		if(color == null)
		{
			return colors.get(0);
		}
		else if(colors.indexOf(color) == colors.size()-1)
		{
			return null;
		}
		
		return colors.get(colors.indexOf(color)+1);
	}
	
	public static void drop(TileEntityLogisticalTransporter tileEntity, TransporterStack stack)
	{
		float[] pos = null;
		
		if(stack.pathToTarget != null)
		{
			pos = TransporterUtils.getStackPosition(tileEntity, stack, 0);
		}
		else {
			pos = new float[] {0, 0, 0};
		}
		
		EntityItem entityItem = new EntityItem(tileEntity.worldObj, tileEntity.xCoord + pos[0], tileEntity.yCoord + pos[1], tileEntity.zCoord + pos[2], stack.itemStack);
		
		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;
	        
        tileEntity.worldObj.spawnEntityInWorld(entityItem);
	}
	
	public static float[] getStackPosition(TileEntityLogisticalTransporter tileEntity, TransporterStack stack, float partial)
	{
		Object3D offset = new Object3D(0, 0, 0).step(ForgeDirection.getOrientation(stack.getSide(tileEntity)));
		float progress = (((float)stack.progress + partial) / 100F) - 0.5F;
		
		float itemFix = 0;
		
		if(stack.itemStack.itemID >= 256)
		{
			itemFix = 0.1F;
		}
		
		return new float[] {0.5F + offset.xCoord*progress, 0.5F + offset.yCoord*progress - itemFix, 0.5F + offset.zCoord*progress};
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