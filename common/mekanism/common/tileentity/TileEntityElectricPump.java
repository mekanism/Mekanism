package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityElectricPump extends TileEntityElectricBlock implements IFluidHandler, ISustainedTank, IEnergySink, IStrictEnergyAcceptor
{
	/** This pump's tank */
	public FluidTank fluidTank;
	
	/** The nodes that have full sources near them or in them */
	public Set<Object3D> recurringNodes = new HashSet<Object3D>();
	
	/** The nodes that have already been sucked up, but are held on to in order to remove dead blocks */
	public Set<Object3D> cleaningNodes = new HashSet<Object3D>();
	
	public TileEntityElectricPump()
	{
		super("Electric Pump", 10000);
		fluidTank = new FluidTank(10000);
		inventory = new ItemStack[3];
	}
	
	@Override
	public void onUpdate()
	{
		ChargeUtils.discharge(2, this);
		
		if(inventory[0] != null)
		{
			if(fluidTank.getFluid() != null && fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				if(FluidContainerRegistry.isEmptyContainer(inventory[0]))
				{
					ItemStack tempStack = FluidContainerRegistry.fillFluidContainer(fluidTank.getFluid(), inventory[0]);
					
					if(tempStack != null)
					{
						if(inventory[1] == null)
						{
							fluidTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[1] = tempStack;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
						else if(tempStack.isItemEqual(inventory[1]) && tempStack.getMaxStackSize() > inventory[1].stackSize)
						{
							fluidTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
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
			if(getEnergy() >= 100 && (fluidTank.getFluid() == null || fluidTank.getFluid().amount+FluidContainerRegistry.BUCKET_VOLUME <= 10000))
			{
				if(suck(true))
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), Object3D.get(this), 50D);
				}
				
				clean(true);
			}
		}
		
		super.onUpdate();
		
		if(fluidTank.getFluid() != null) 
		{
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS) 
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(orientation).getTileEntity(worldObj);

				if(tileEntity instanceof IFluidHandler) 
				{
					fluidTank.drain(((IFluidHandler)tileEntity).fill(orientation.getOpposite(), fluidTank.getFluid(), true), true);
					
					if(fluidTank.getFluid() == null || fluidTank.getFluid().amount <= 0) 
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
			
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - 100);
						recurringNodes.add(new Object3D(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord));
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}
					
					return true;
				}
			}
		}
		
		for(Object3D wrapper : cleaningNodes)
		{
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() != null && MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(getEnergy() - 100);
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
						worldObj.setBlockToAir(wrapper.xCoord, wrapper.yCoord, wrapper.zCoord);
					}
					
					return true;
				}
			}
		}
		
		for(Object3D wrapper : tempPumpList)
		{
			if(MekanismUtils.isFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
			{
				if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord).isFluidEqual(fluidTank.getFluid()))
				{
					if(take)
					{
						setEnergy(electricityStored - 100);
						fluidTank.fill(MekanismUtils.getFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord), true);
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
					if(MekanismUtils.isFluid(worldObj, side.xCoord, side.yCoord, side.zCoord))
					{
						if(fluidTank.getFluid() == null || MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord).isFluidEqual(fluidTank.getFluid()))
						{
							if(take)
							{
								setEnergy(electricityStored - 100);
								recurringNodes.add(side);
								fluidTank.fill(MekanismUtils.getFluid(worldObj, side.xCoord, side.yCoord, side.zCoord), true);
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
				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
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
				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
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
				
				if(MekanismUtils.isDeadFluid(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord))
				{
					if(fluidTank.getFluid() != null && MekanismUtils.getFluidId(worldObj, wrapper.xCoord, wrapper.yCoord, wrapper.zCoord) == fluidTank.getFluid().fluidID)
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
			fluidTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(fluidTank.getFluid() != null)
		{
			data.add(1);
			data.add(fluidTank.getFluid().fluidID);
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(0);
		}
		
		return data;
	}
	
	public int getScaledFluidLevel(int i)
	{
		return fluidTank.getFluid() != null ? fluidTank.getFluid().amount*i / 10000 : 0;
	}
	
    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(fluidTank.getFluid() != null)
        {
        	nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        }
        
        NBTTagList recurringList = new NBTTagList();
        
        for(Object3D wrapper : recurringNodes)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	wrapper.write(tagCompound);
        	recurringList.appendTag(tagCompound);
        }
        
        if(recurringList.tagCount() != 0)
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
        
        if(cleaningList.tagCount() != 0)
        {
        	nbtTags.setTag("cleaningNodes", cleaningList);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	if(nbtTags.hasKey("fluidTank"))
    	{
    		fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
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
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return false;
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 2)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return true;
	}
    
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
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
    	double neededElectricity = getMaxEnergy()-getEnergy();
    	
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
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return direction != ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
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
	public FluidTankInfo[] getTankInfo(ForgeDirection direction) 
	{
		return new FluidTankInfo[] {fluidTank.getInfo()};
	}

	@Override
	public void setFluidStack(FluidStack fluidStack, Object... data) 
	{
		fluidTank.setFluid(fluidStack);
	}

	@Override
	public FluidStack getFluidStack(Object... data) 
	{
		return fluidTank.getFluid();
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return true;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		if(fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() != resource.getFluid())
		{
			return drain(from, resource.amount, doDrain);
		}
		
		return null;
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return fluidTank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return true;
	}
}
