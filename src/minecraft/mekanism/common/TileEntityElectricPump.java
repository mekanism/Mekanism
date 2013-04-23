package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import buildcraft.api.core.Position;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import mekanism.api.Object3D;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.InfusionType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityElectricPump extends TileEntityElectricBlock implements ITankContainer, ISustainedTank, IEnergySink, IStrictEnergyAcceptor
{
	/** This pump's tank */
	public LiquidTank liquidTank;
	
	/** The nodes that have full sources near them or in them */
	public Set<Object3D> recurringNodes = new HashSet<Object3D>();
	
	/** The nodes that have already been sucked up, but are held on to in order to remove dead blocks */
	public Set<Object3D> cleaningNodes = new HashSet<Object3D>();
	
	public TileEntityElectricPump()
	{
		super("Electric Pump", 10000);
		liquidTank = new LiquidTank(10000);
		inventory = new ItemStack[3];
	}
	
	@Override
	public void onUpdate()
	{
		ChargeUtils.discharge(2, this);
		
		if(inventory[0] != null)
		{
			if(liquidTank.getLiquid() != null && liquidTank.getLiquid().amount >= LiquidContainerRegistry.BUCKET_VOLUME)
			{
				if(LiquidContainerRegistry.isEmptyContainer(inventory[0]))
				{
					ItemStack tempStack = LiquidContainerRegistry.fillLiquidContainer(liquidTank.getLiquid(), inventory[0]);
					
					if(tempStack != null)
					{
						if(inventory[1] == null)
						{
							liquidTank.drain(LiquidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[1] = tempStack;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
						else if(tempStack.isItemEqual(inventory[1]) && tempStack.getMaxStackSize() > inventory[1].stackSize)
						{
							liquidTank.drain(LiquidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[1].stackSize++;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}
		}
		
		if(!worldObj.isRemote && worldObj.getWorldTime() % 20 == 0)
		{
			if(electricityStored >= 100 && (liquidTank.getLiquid() == null || liquidTank.getLiquid().amount+LiquidContainerRegistry.BUCKET_VOLUME <= 10000))
			{
				if(suck(true))
				{
					clean(true);
					PacketHandler.sendTileEntityPacketToClients(this, 50, getNetworkedData(new ArrayList()));
				}
				else {
					clean(true);
					cleaningNodes.clear();
				}
			}
		}
		
		super.onUpdate();
		
		if(liquidTank.getLiquid() != null) 
		{
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS) 
			{
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(xCoord, yCoord, zCoord), orientation);

				if(tileEntity instanceof ITankContainer) 
				{
					liquidTank.drain(((ITankContainer)tileEntity).fill(orientation.getOpposite(), liquidTank.getLiquid(), true), true);
					
					if(liquidTank.getLiquid() == null || liquidTank.getLiquid().amount <= 0) 
					{
						break;
					}
				}
			}
		}
	}
	
	public boolean suck(boolean take)
	{
		List<Object3D> tempPumpList = Arrays.asList(recurringNodes.toArray(new Object3D[recurringNodes.size()]));
		Collections.shuffle(tempPumpList);
		
		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			Object3D wrapper = Object3D.get(this).getFromSide(orientation);
			
			if(MekanismUtils.isLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isLiquidEqual(liquidTank.getLiquid()))
				{
					if(take)
					{
						setEnergy(electricityStored - 100);
						recurringNodes.add(new Object3D(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord));
						liquidTank.fill(MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}
					
					return true;
				}
			}
		}
		
		for(Object3D wrapper : cleaningNodes)
		{
			if(MekanismUtils.isLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(liquidTank.getLiquid() != null && MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isLiquidEqual(liquidTank.getLiquid()))
				{
					if(take)
					{
						setEnergy(electricityStored - 100);
						liquidTank.fill(MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}
					
					return true;
				}
			}
		}
		
		for(Object3D wrapper : tempPumpList)
		{
			if(MekanismUtils.isLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isLiquidEqual(liquidTank.getLiquid()))
				{
					if(take)
					{
						setEnergy(electricityStored - 100);
						liquidTank.fill(MekanismUtils.getLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}
					
					return true;
				}
			}
			
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D side = wrapper.getFromSide(orientation);
				
				if(Object3D.get(this).distanceTo(side) <= 80)
				{
					if(MekanismUtils.isLiquid(worldObj, side.xCoord, side.yCoord, side.zCoord))
					{
						if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, side.xCoord, side.yCoord, side.zCoord).isLiquidEqual(liquidTank.getLiquid()))
						{
							if(take)
							{
								setEnergy(electricityStored - 100);
								recurringNodes.add(side);
								liquidTank.fill(MekanismUtils.getLiquid(worldObj, side.xCoord, side.yCoord, side.zCoord), true);
								worldObj.setBlockToAir(side.xCoord, side.yCoord, side.zCoord);
							}
							
							return true;
						}
					}
				}
			}
			
			cleaningNodes.add(wrapper);
			recurringNodes.remove(wrapper);
		}
		
		return false;
	}
	
	public boolean clean(boolean take)
	{
		boolean took = false;
		if(!worldObj.isRemote)
		{
			for(Object3D wrapper : cleaningNodes)
			{
				if(MekanismUtils.isDeadLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == liquidTank.getLiquid().itemID)
					{
						took = true;
						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}
			
			for(Object3D wrapper : recurringNodes)
			{
				if(MekanismUtils.isDeadLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == liquidTank.getLiquid().itemID)
					{
						took = true;
						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}
			
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D wrapper = Object3D.get(this).getFromSide(orientation);
				
				if(MekanismUtils.isDeadLiquid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == liquidTank.getLiquid().itemID)
					{
						took = true;
						if(take)
						{
							worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
						}
					}
				}
			}
		}
		
		return took;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(dataStream.readInt() == 1)
		{
			int amount = dataStream.readInt();
			int itemID = dataStream.readInt();
			int itemMeta = dataStream.readInt();
		
			liquidTank.setLiquid(new LiquidStack(itemID, amount, itemMeta));
		}
		else {
			liquidTank.setLiquid(null);
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(liquidTank.getLiquid() != null)
		{
			data.add(1);
			data.add(liquidTank.getLiquid().amount);
			data.add(liquidTank.getLiquid().itemID);
			data.add(liquidTank.getLiquid().itemMeta);
		}
		else {
			data.add(0);
		}
		
		return data;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	public int getScaledLiquidLevel(int i)
	{
		return liquidTank.getLiquid() != null ? liquidTank.getLiquid().amount*i / 10000 : 0;
	}
	
    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(liquidTank.getLiquid() != null)
        {
        	nbtTags.setTag("liquidTank", liquidTank.writeToNBT(new NBTTagCompound()));
        }
        
        NBTTagList recurringList = new NBTTagList();
        
        for(Object3D wrapper : recurringNodes)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	wrapper.write(tagCompound);
        	recurringList.appendTag(tagCompound);
        }
        
        if(!recurringList.tagList.isEmpty())
        {
        	nbtTags.setTag("recurringNodes", recurringList);
        }
        
        NBTTagList cleaningList = new NBTTagList();
        
        for(Object3D wrapper : cleaningNodes)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	wrapper.write(tagCompound);
        	cleaningList.appendTag(tagCompound);
        }
        
        if(!cleaningList.tagList.isEmpty())
        {
        	nbtTags.setTag("cleaningNodes", cleaningList);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	if(nbtTags.hasKey("liquidTank"))
    	{
    		liquidTank.readFromNBT(nbtTags.getCompoundTag("liquidTank"));
    	}
    	
    	if(nbtTags.hasKey("recurringNodes"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("recurringNodes");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			recurringNodes.add(Object3D.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    	
    	if(nbtTags.hasKey("cleaningNodes"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("cleaningNodes");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			cleaningNodes.add(Object3D.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }
    
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return false;
		}
		else if(slotID == 0)
		{
			return LiquidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 2)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		return true;
	}
    
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
		}
		else if(slotID == 1)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = MAX_ELECTRICITY-electricityStored;
    	
    	if(amount <= neededElectricity)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededElectricity;
    		rejects = amount-neededElectricity;
    	}
    	
    	return rejects;
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return true;
	}
	
	@Override
	public int demandsEnergy() 
	{
		return (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
    	double rejects = 0;
    	double neededEnergy = MAX_ELECTRICITY-electricityStored;
    	if(i <= neededEnergy)
    	{
    		electricityStored += i;
    	}
    	else if(i > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = i-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return drain(0, maxDrain, doDrain);
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		if(side == 1)
		{
			return new int[] {0};
		}
		else if(side == 0)
		{
			return new int[] {1};
		}
		else {
			return new int[] {2};
		}
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		if(tankIndex == 0)
		{
			return liquidTank.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) 
	{
		return new ILiquidTank[] {liquidTank};
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) 
	{
		return liquidTank;
	}

	@Override
	public void setLiquidStack(LiquidStack liquidStack, Object... data) 
	{
		liquidTank.setLiquid(liquidStack);
	}

	@Override
	public LiquidStack getLiquidStack(Object... data) 
	{
		return liquidTank.getLiquid();
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return true;
	}
}
