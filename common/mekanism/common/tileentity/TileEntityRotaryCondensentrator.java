package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.GasUtils;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.IActiveState;
import mekanism.common.ISustainedTank;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityRotaryCondensentrator extends TileEntityElectricBlock implements IActiveState, ISustainedTank, IFluidHandler, IGasStorage, IGasAcceptor, ITubeConnection
{
	public GasStack gasTank;
	
	public FluidTank fluidTank;
	
	public static final int MAX_GAS = 10000;
	
	public int updateDelay;
	
	/** 0: gas -> fluid; 1: fluid -> gas */
	public int mode;
	
	public int gasOutput = 16;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public TileEntityRotaryCondensentrator()
	{
		super("RotaryCondensentrator", MachineType.ROTARY_CONDENSENTRATOR.baseEnergy);
		fluidTank = new FluidTank(10000);
		inventory = new ItemStack[5];
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;
				
				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
				}
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;
					
				if(updateDelay == 0 && clientActive != isActive)
				{
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				}
			}
			
			ChargeUtils.discharge(4, this);
			
			if(mode == 0)
			{
				if(inventory[1] != null && (getGas() == null || getGas().amount < getMaxGas()))
				{
					if(getGas() == null)
					{
						setGas(GasUtils.removeGas(inventory[1], null, getMaxGas()));
					}
					else {
						GasStack removed = GasUtils.removeGas(inventory[1], getGas().getGas(), getMaxGas()-getGas().amount);
						setGas(new GasStack(getGas().getGas(), getGas().amount + (removed != null ? removed.amount : 0)));
					}
				}
				
				if(inventory[2] != null)
				{
					if(FluidContainerRegistry.isEmptyContainer(inventory[2]))
					{
						if(fluidTank.getFluid() != null && fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
						{
							ItemStack filled = FluidContainerRegistry.fillFluidContainer(fluidTank.getFluid(), inventory[2]);
							
							if(filled != null)
							{
								if(inventory[3] == null || (inventory[3].isItemEqual(filled) && inventory[3].stackSize+1 <= filled.getMaxStackSize()))
								{
									inventory[2].stackSize--;
									
									if(inventory[2].stackSize <= 0)
									{
										inventory[2] = null;
									}
									
									if(inventory[3] == null)
									{
										inventory[3] = filled;
									}
									else {
										inventory[3].stackSize++;
									}
									
									fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);
								}
							}
						}
					}
				}
			}
			else if(mode == 1)
			{
				if(getGas() != null)
				{
					if(inventory[0] != null)
					{
						setGas(new GasStack(getGas().getGas(), getGas().amount - GasUtils.addGas(inventory[0], getGas())));
					}
				}
				
				if(getGas() != null)
				{
					GasStack toSend = new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput));
					setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getLeft(facing))));
					
					TileEntity tileEntity = Object3D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);
					
					if(tileEntity instanceof IGasAcceptor)
					{
						if(((IGasAcceptor)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), getGas().getGas()))
						{
							int added = ((IGasAcceptor)tileEntity).receiveGas(new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput)));
							
							setGas(new GasStack(getGas().getGas(), getGas().amount - added));
						}
					}
				}
				
				if(FluidContainerRegistry.isFilledContainer(inventory[2]))
				{
					FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(inventory[2]);
					
					if((fluidTank.getFluid() == null && itemFluid.amount <= 10000) || fluidTank.getFluid().amount+itemFluid.amount <= 10000)
					{
						if(fluidTank.getFluid() != null && !fluidTank.getFluid().isFluidEqual(itemFluid))
						{
							return;
						}
						
						ItemStack containerItem = inventory[2].getItem().getContainerItemStack(inventory[2]);
						
						boolean filled = false;
						
						if(containerItem != null)
						{
							if(inventory[3] == null || (inventory[3].isItemEqual(containerItem) && inventory[3].stackSize+1 <= containerItem.getMaxStackSize()))
							{
								inventory[2] = null;
								
								if(inventory[3] == null)
								{
									inventory[3] = containerItem;
								}
								else {
									inventory[3].stackSize++;
								}
								
								filled = true;
							}
						}
						else {						
							inventory[2].stackSize--;
							
							if(inventory[2].stackSize == 0)
							{
								inventory[2] = null;
							}
							
							filled = true;
						}
						
						if(filled)
						{
							fluidTank.fill(itemFluid, true);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				mode = mode == 0 ? 1 : 0;
			}
			
			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		mode = dataStream.readInt();
		
		if(dataStream.readBoolean())
		{
			fluidTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			gasTank = new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt());
		}
		else {
			gasTank = null;
		}
		
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(mode);
		
		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().fluidID);
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		if(gasTank != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getID());
			data.add(gasTank.amount);
		}
		else {
			data.add(false);
		}
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        mode = nbtTags.getInteger("mode");
        gasTank = GasStack.readFromNBT(nbtTags.getCompoundTag("gasTank"));
        
    	if(nbtTags.hasKey("fluidTank"))
    	{
    		fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
    	}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("mode", mode);
        
        if(gasTank != null)
        {
        	nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
        }
        
        if(fluidTank.getFluid() != null)
        {
        	nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        }
    }
	
	public int getScaledFluidLevel(int i)
	{
		return fluidTank.getFluid() != null ? fluidTank.getFluid().amount*i / 10000 : 0;
	}
	
	public int getScaledGasLevel(int i)
	{
		return gasTank != null ? gasTank.amount*i / MAX_GAS : 0;
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(clientActive != active && updateDelay == 0)
    	{
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
    		
    		updateDelay = 10;
    		clientActive = active;
    	}
    }
    
    @Override
    public boolean getActive()
    {
    	return isActive;
    }
    
    @Override
    public boolean renderUpdate()
    {
    	return false;
    }
    
    @Override
    public boolean lightUpdate()
    {
    	return true;
    }

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == MekanismUtils.getLeft(facing);
	}

	@Override
	public int receiveGas(GasStack stack)
	{
		if(gasTank == null || (gasTank != null && gasTank.getGas() == stack.getGas()))
		{
			int stored = getGas() != null ? getGas().amount : 0;
			int toUse = Math.min(getMaxGas()-stored, stack.amount);
			
			setGas(new GasStack(stack.getGas(), stored + toUse));
	    	
	    	return toUse;
		}
		
		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return mode == 0 && (getGas() == null || getGas().getGas() == type) && side == MekanismUtils.getLeft(facing);
	}

	@Override
	public GasStack getGas(Object... data)
	{
		return gasTank;
	}

	@Override
	public void setGas(GasStack stack, Object... data)
	{
		if(stack == null || stack.amount == 0)
		{
			gasTank = null;
		}
		else {
			gasTank = new GasStack(stack.getGas(), Math.max(Math.min(stack.amount, getMaxGas()), 0));
		}
		
		MekanismUtils.saveChunk(this);
	}

	@Override
	public int getMaxGas(Object... data)
	{
		return MAX_GAS;
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
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(canFill(from, resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(fluidTank.getFluid() != null && fluidTank.getFluid().getFluid() == resource.getFluid())
		{
			return drain(from, resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return mode == 1 && from == MekanismUtils.getRight(facing);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return mode == 0 && from == MekanismUtils.getRight(facing);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(from == MekanismUtils.getRight(facing))
		{
			return new FluidTankInfo[] {fluidTank.getInfo()};
		}
		
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(canDrain(from, null))
		{
			return fluidTank.drain(maxDrain, doDrain);
		}
		
		return null;
	}
}
