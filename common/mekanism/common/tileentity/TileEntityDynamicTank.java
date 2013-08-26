package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.SynchronizedTankData;
import mekanism.common.TankUpdateProtocol;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.SynchronizedTankData.ValveData;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDynamicTank extends TileEntityContainerBlock
{
	/** Unique inventory ID for the dynamic tank, serves as a way to retrieve cached inventories. */
	public int inventoryID = -1;
	
	/** The tank data for this structure. */
	public SynchronizedTankData structure;
	
	/** Whether or not to send this tank's structure in the next update packet. */
	public boolean sendStructure;
	
	/** This tank's previous "has structure" state. */
	public boolean prevStructure;
	
	/** Whether or not this tank has it's structure, for the client side mechanics. */
	public boolean clientHasStructure;
	
	/** The cached fluid this tank segment contains. */
	public FluidStack cachedFluid;
	
	/** A client-sided and server-sided map of valves on this tank's structure, used on the client for rendering fluids. */
	public Map<ValveData, Integer> valveViewing = new HashMap<ValveData, Integer>();
	
	/** The capacity this tank has on the client-side. */
	public int clientCapacity;
	
	/** Whether or not this tank segment is rendering the structure. */
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
		if(!worldObj.isRemote && (structure == null || !structure.didTick))
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
				
				if(!prevStructure)
				{
					Mekanism.proxy.doTankAnimation(this);
				}
			}
			
			prevStructure = clientHasStructure;
			
			if(!clientHasStructure || !isRendering)
			{
				for(ValveData data : valveViewing.keySet())
				{
					TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)data.location.getTileEntity(worldObj);
					
					if(tileEntity != null)
					{
						tileEntity.clientHasStructure = false;
					}
				}
				
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
				MekanismUtils.updateCache(inventoryID, cachedFluid, inventory, this);
			}
			
			if(structure == null && packetTick == 5)
			{
				update();
			}
			
			if(structure != null && isRendering && packetTick % 20 == 0)
			{
				sendStructure = true;
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), Object3D.get(this), 50D);
			}
			
			if(prevStructure != (structure != null))
			{
				if(structure != null && !structure.hasRenderer)
				{
					structure.hasRenderer = true;
					isRendering = true;
					sendStructure = true;
				}
				
				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
			}
			
			prevStructure = structure != null;
			
			if(structure != null)
			{
				structure.didTick = false;
				
				if(inventoryID != -1)
				{
					MekanismUtils.updateCache(inventoryID, structure.fluidStored, structure.inventory, this);
					
					cachedFluid = structure.fluidStored;
					inventory = structure.inventory;
				}
				
				manageInventory();
			}
		}
	}
	
	@Override
	public void validate()
	{
		//no super for no packets!
		tileEntityInvalid = false;
	}
	
	public void manageInventory()
	{
		int max = structure.volume*16000;
		
		if(structure.inventory[0] != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(structure.inventory[0]))
			{
				if(structure.fluidStored != null && structure.fluidStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(structure.fluidStored, structure.inventory[0]);
					
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
							
							structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;
							
							if(structure.fluidStored.amount == 0)
							{
								structure.fluidStored = null;
							}
							
							PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(structure.inventory[0]))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(structure.inventory[0]);
				
				if((structure.fluidStored == null && itemFluid.amount <= max) || structure.fluidStored.amount+itemFluid.amount <= max)
				{
					if(structure.fluidStored != null && !structure.fluidStored.isFluidEqual(itemFluid))
					{
						return;
					}
					
					ItemStack bucket = FluidContainerRegistry.isBucket(structure.inventory[0]) ? new ItemStack(Item.bucketEmpty) : null;
					
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
						if(structure.fluidStored == null)
						{
							structure.fluidStored = itemFluid.copy();
						}
						else {
							structure.fluidStored.amount += itemFluid.amount;
						}
					}
					
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				}
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(isRendering);
		data.add(structure != null);
		data.add(structure != null ? structure.volume*16000 : 0);
		
		if(structure != null && structure.fluidStored != null)
		{
			data.add(1);
			data.add(structure.fluidStored.fluidID);
			data.add(structure.fluidStored.amount);
		}
		else {
			data.add(0);
		}
		
		if(structure != null && isRendering)
		{
			if(sendStructure)
			{
				sendStructure = false;
				
				data.add(true);
				
				data.add(structure.volHeight);
				data.add(structure.volWidth);
				data.add(structure.volLength);
				data.add(structure.renderLocation.xCoord);
				data.add(structure.renderLocation.yCoord);
				data.add(structure.renderLocation.zCoord);
			}
			else {
				data.add(false);
			}
			
			data.add(structure.valves.size());
			
			for(ValveData valveData : structure.valves)
			{
				data.add(valveData.location.xCoord);
				data.add(valveData.location.yCoord);
				data.add(valveData.location.zCoord);
				
				data.add(valveData.side.ordinal());
				data.add(valveData.serverFluid);
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
		
		clientCapacity = dataStream.readInt();
		
		if(dataStream.readInt() == 1)
		{
			structure.fluidStored = new FluidStack(dataStream.readInt(), dataStream.readInt());
		}
		else {
			structure.fluidStored = null;
		}
		
		if(clientHasStructure && isRendering)
		{
			if(dataStream.readBoolean())
			{
				structure.volHeight = dataStream.readInt();
				structure.volWidth = dataStream.readInt();
				structure.volLength = dataStream.readInt();
				structure.renderLocation = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
			}
			
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
				
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)data.location.getTileEntity(worldObj);
				
				if(tileEntity != null)
				{
					tileEntity.clientHasStructure = true;
				}
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
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
				}
			}
		}
	}
	
	public int getScaledFluidLevel(int i)
	{
		if(clientCapacity == 0 || structure.fluidStored == null)
		{
			return 0;
		}
		
		return structure.fluidStored.amount*i / clientCapacity;
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
        
        if(structure == null)
        {
	        inventoryID = nbtTags.getInteger("inventoryID");
	
	        if(inventoryID != -1)
	        {
		        if(nbtTags.hasKey("cachedFluid"))
		        {
		        	cachedFluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedFluid"));
		        }
	        }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("inventoryID", inventoryID);
        
        if(cachedFluid != null)
        {
        	nbtTags.setTag("cachedFluid", cachedFluid.writeToNBT(new NBTTagCompound()));
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
