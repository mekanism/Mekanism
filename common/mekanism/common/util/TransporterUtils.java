package mekanism.common.util;

import java.util.ArrayList;
import java.util.Arrays;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.common.tileentity.TileEntityBin;
import mekanism.common.tileentity.TileEntityDiversionTransporter;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.transporter.TransporterStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
				
				if(tileEntity instanceof TileEntityDiversionTransporter)
				{
					int mode = ((TileEntityDiversionTransporter)tileEntity).modes[side];
					boolean redstone = tileEntity.worldObj.isBlockIndirectlyGettingPowered(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
					
					if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
					{
						continue;
					}
				}
				
				ForgeDirection forgeSide = ForgeDirection.getOrientation(side).getOpposite();
				
				//Immature BuildCraft inv check
				if(MekanismUtils.useBuildcraft() && inventory instanceof IPowerReceptor)
				{
					if(((IPowerReceptor)inventory).getPowerReceiver(forgeSide) != null && ((IPowerReceptor)inventory).getPowerReceiver(forgeSide).getType() == Type.MACHINE)
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
				
				if(tileEntity instanceof TileEntityDiversionTransporter)
				{
					int mode = ((TileEntityDiversionTransporter)tileEntity).modes[side];
					boolean redstone = tileEntity.worldObj.isBlockIndirectlyGettingPowered(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
					
					if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
					{
						continue;
					}
				}
				
				if(tile instanceof TileEntityDiversionTransporter)
				{
					int mode = ((TileEntityDiversionTransporter)tile).modes[ForgeDirection.VALID_DIRECTIONS[side].getOpposite().ordinal()];
					boolean redstone = tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
					
					if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
					{
						continue;
					}
				}
				
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
    		inventory = InventoryUtils.checkChestInv(inventory);
    		
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
				if(force && sidedInventory instanceof TileEntityBin && ForgeDirection.getOrientation(side).getOpposite().ordinal() == 0)
				{
					slots = sidedInventory.getAccessibleSlotsFromSide(1);
				}
				
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