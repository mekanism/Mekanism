package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.SynchronizedTankData.ValveData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDynamicTank extends TileEntityContainerBlock
{
	/** Unique inventory ID for the dynamic tank, serves as a way to retrieve cached inventories. */
	public int inventoryID = -1;
	
	/** The tank data for this structure. */
	public SynchronizedTankData structure;
	
	public boolean prevStructure;
	
	public boolean clientHasStructure;
	
	public LiquidStack cachedLiquid;
	
	public Map<ValveData, Integer> valveViewing = new HashMap<ValveData, Integer>();
	
	public int clientCapacity;
	
	public boolean isRendering;
	
	public TileEntityDynamicTank()
	{
		this("Dynamic Tank");
	}
	
	public TileEntityDynamicTank(String name)
	{
		super(name);
		inventory = new ItemStack[2];
	}
	
	public void update()
	{
		if(!worldObj.isRemote && canUpdateData())
		{
			new TankUpdateProtocol(this).updateTanks();
			
			if(structure != null)
			{
				structure.didTick = true;
			}
		}
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = new SynchronizedTankData();
			}
			
			if(structure != null && clientHasStructure && isRendering)
			{
				for(ValveData data : valveViewing.keySet())
				{
					if(valveViewing.get(data) > 0)
					{
						valveViewing.put(data, valveViewing.get(data)-1);
					}
				}
			}
			
			if(!clientHasStructure || !isRendering)
			{
				valveViewing.clear();
			}
		}
		
		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				player.closeScreen();
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(structure == null)
			{
				isRendering = false;
			}
			
			if(inventoryID != -1 && structure == null)
			{
				MekanismUtils.updateCache(inventoryID, cachedLiquid, inventory, this);
			}
			
			if(structure == null && packetTick == 5)
			{
				update();
			}
			
			if(prevStructure != (structure != null))
			{
				PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
			}
			
			prevStructure = structure != null;
			
			if(structure != null)
			{
				structure.didTick = false;
				
				if(!structure.hasRenderer)
				{
					structure.hasRenderer = true;
					isRendering = true;
					
					PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
				}
				
				if(inventoryID != -1)
				{
					MekanismUtils.updateCache(inventoryID, structure.liquidStored, structure.inventory, this);
					
					cachedLiquid = structure.liquidStored;
					inventory = structure.inventory;
				}
				
				manageInventory();
			}
		}
	}
	
	public void manageInventory()
	{
		int max = structure.volume*16000;
		
		if(structure.inventory[0] != null)
		{
			if(LiquidContainerRegistry.isEmptyContainer(structure.inventory[0]))
			{
				if(structure.liquidStored != null && structure.liquidStored.amount >= LiquidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = LiquidContainerRegistry.fillLiquidContainer(structure.liquidStored, structure.inventory[0]);
					
					if(filled != null)
					{
						if(structure.inventory[1] == null || (structure.inventory[1].isItemEqual(filled) && structure.inventory[1].stackSize+1 <= filled.getMaxStackSize()))
						{
							structure.inventory[0].stackSize--;
							
							if(structure.inventory[0].stackSize <= 0)
							{
								structure.inventory[0] = null;
							}
							
							if(structure.inventory[1] == null)
							{
								structure.inventory[1] = filled;
							}
							else {
								structure.inventory[1].stackSize++;
							}
							
							structure.liquidStored.amount -= LiquidContainerRegistry.getLiquidForFilledItem(filled).amount;
							
							if(structure.liquidStored.amount == 0)
							{
								structure.liquidStored = null;
							}
							
							PacketHandler.sendTileEntityPacketToClients(this, 50, getNetworkedData(new ArrayList()));
						}
					}
				}
			}
			else if(LiquidContainerRegistry.isFilledContainer(structure.inventory[0]))
			{
				LiquidStack itemLiquid = LiquidContainerRegistry.getLiquidForFilledItem(structure.inventory[0]);
				
				if((structure.liquidStored == null && itemLiquid.amount <= max) || structure.liquidStored.amount+itemLiquid.amount <= max)
				{
					if(structure.liquidStored != null && !structure.liquidStored.isLiquidEqual(itemLiquid))
					{
						return;
					}
					
					ItemStack bucket = LiquidContainerRegistry.isBucket(structure.inventory[0]) ? new ItemStack(Item.bucketEmpty) : null;
					
					boolean filled = false;
					
					if(bucket != null)
					{
						if(structure.inventory[1] == null || (structure.inventory[1].isItemEqual(bucket) && structure.inventory[1].stackSize+1 <= bucket.getMaxStackSize()))
						{
							structure.inventory[0] = null;
							
							if(structure.inventory[1] == null)
							{
								structure.inventory[1] = bucket;
							}
							else {
								structure.inventory[1].stackSize++;
							}
							
							filled = true;
						}
					}
					else {						
						structure.inventory[0].stackSize--;
						
						if(structure.inventory[0].stackSize == 0)
						{
							structure.inventory[0] = null;
						}
						
						filled = true;
					}
					
					if(filled)
					{
						if(structure.liquidStored == null)
						{
							structure.liquidStored = itemLiquid.copy();
						}
						else {
							structure.liquidStored.amount += itemLiquid.amount;
						}
					}
					
					PacketHandler.sendTileEntityPacketToClients(this, 50, getNetworkedData(new ArrayList()));
				}
			}
		}
	}
	
	public boolean canUpdateData()
	{
		return structure == null || !structure.didTick;
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(isRendering);
		data.add(structure != null);
		
		if(structure != null)
		{
			data.add(structure.volHeight);
			data.add(structure.volWidth);
			data.add(structure.volLength);
			data.add(structure.renderLocation.xCoord);
			data.add(structure.renderLocation.yCoord);
			data.add(structure.renderLocation.zCoord);
		}
		
		data.add(structure != null ? structure.volume*16000 : 0);
		
		if(structure != null && structure.liquidStored != null)
		{
			data.add(1);
			data.add(structure.liquidStored.itemID);
			data.add(structure.liquidStored.amount);
			data.add(structure.liquidStored.itemMeta);
		}
		else {
			data.add(0);
		}
		
		if(structure != null)
		{
			data.add(structure.valves.size());
			
			for(ValveData valveData : structure.valves)
			{
				data.add(valveData.location.xCoord);
				data.add(valveData.location.yCoord);
				data.add(valveData.location.zCoord);
				
				data.add(valveData.side.ordinal());
				data.add(valveData.serverLiquid);
			}
		}
		
		return data;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(structure == null)
		{
			structure = new SynchronizedTankData();
		}
		
		isRendering = dataStream.readBoolean();
		clientHasStructure = dataStream.readBoolean();
		
		if(clientHasStructure)
		{
			structure.volHeight = dataStream.readInt();
			structure.volWidth = dataStream.readInt();
			structure.volLength = dataStream.readInt();
			structure.renderLocation = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		}
		
		clientCapacity = dataStream.readInt();
		
		if(dataStream.readInt() == 1)
		{
			structure.liquidStored = new LiquidStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		}
		else {
			structure.liquidStored = null;
		}
		
		if(clientHasStructure)
		{
			int size = dataStream.readInt();
			
			for(int i = 0; i < size; i++)
			{
				ValveData data = new ValveData();
				data.location = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
				data.side = ForgeDirection.getOrientation(dataStream.readInt());
				int viewingTicks = 0;
				
				if(dataStream.readBoolean())
				{
					viewingTicks = 30;
				}
				
				if(viewingTicks == 0)
				{
					if(valveViewing.containsKey(data) && valveViewing.get(data) > 0)
					{
						continue;
					}
				}
				
				valveViewing.put(data, viewingTicks);
			}
		}
	}
	
	public void sendPacketToRenderer()
	{
		if(structure != null)
		{
			for(Object3D obj : structure.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(worldObj);
				
				if(tileEntity != null && tileEntity.isRendering)
				{
					PacketHandler.sendTileEntityPacketToClients(tileEntity, 50, tileEntity.getNetworkedData(new ArrayList()));
				}
			}
		}
	}
	
	public int getScaledLiquidLevel(int i)
	{
		if(clientCapacity == 0 || structure.liquidStored == null)
		{
			return 0;
		}
		
		return structure.liquidStored.amount*i / clientCapacity;
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		return structure != null ? structure.inventory[slotID] : null;
	}
	
	@Override
    public void setInventorySlotContents(int slotID, ItemStack itemstack)
    {
		if(structure != null)
		{
	        structure.inventory[slotID] = itemstack;
	
	        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
	        {
	            itemstack.stackSize = getInventoryStackLimit();
	        }
		}
    }
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        inventoryID = nbtTags.getInteger("inventoryID");

        if(inventoryID != -1)
        {
	        if(nbtTags.hasKey("cachedLiquid"))
	        {
	        	cachedLiquid = LiquidStack.loadLiquidStackFromNBT(nbtTags.getCompoundTag("cachedLiquid"));
	        }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("inventoryID", inventoryID);
        
        if(cachedLiquid != null)
        {
        	nbtTags.setTag("cachedLiquid", cachedLiquid.writeToNBT(new NBTTagCompound()));
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
