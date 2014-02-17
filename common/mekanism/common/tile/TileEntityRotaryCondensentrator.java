package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
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

public class TileEntityRotaryCondensentrator extends TileEntityElectricBlock implements IActiveState, ISustainedTank, IFluidHandler, IGasHandler, ITubeConnection, IRedstoneControl
{
	public GasTank gasTank = new GasTank(MAX_GAS);
	
	public FluidTank fluidTank;
	
	public static final int MAX_GAS = 10000;
	
	public int updateDelay;
	
	/** 0: gas -> fluid; 1: fluid -> gas */
	public int mode;
	
	public int gasOutput = 16;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public double prevEnergy;
	
	public final double ENERGY_USAGE = Mekanism.rotaryCondensentratorUsage;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
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
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
				}
			}
			
			ChargeUtils.discharge(4, this);
			
			if(mode == 0)
			{
				if(inventory[1] != null && (gasTank.getGas() == null || gasTank.getStored() < gasTank.getMaxGas()))
				{
					gasTank.receive(GasTransmission.removeGas(inventory[1], null, gasTank.getNeeded()), true);
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
				
				if(getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this) && isValidGas(gasTank.getGas()) && (fluidTank.getFluid() == null || (fluidTank.getFluid().amount < 10000 && gasEquals(gasTank.getGas(), fluidTank.getFluid()))))
				{
					setActive(true);
					fluidTank.fill(new FluidStack(gasTank.getGas().getGas().getFluid(), 1), true);
					gasTank.draw(1, true);
					setEnergy(getEnergy() - ENERGY_USAGE);
				}
				else {
					if(prevEnergy >= getEnergy())
					{
						setActive(false);
					}
				}
			}
			else if(mode == 1)
			{
				if(inventory[0] != null && gasTank.getGas() != null)
				{
					gasTank.draw(GasTransmission.addGas(inventory[0], gasTank.getGas()), true);
				}
				
				if(gasTank.getGas() != null)
				{
					GasStack toSend = new GasStack(gasTank.getGas().getGas(), Math.min(gasTank.getGas().amount, gasOutput));
					gasTank.draw(GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getLeft(facing)), true);
					
					TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);
					
					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), gasTank.getGas().getGas()))
						{
							gasTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getLeft(facing).getOpposite(), toSend), true);
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
				
				if(getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this) && isValidFluid(fluidTank.getFluid()) && (gasTank.getGas() == null || (gasTank.getStored() < MAX_GAS && gasEquals(gasTank.getGas(), fluidTank.getFluid()))))
				{
					setActive(true);
					gasTank.receive(new GasStack(GasRegistry.getGas(fluidTank.getFluid().getFluid()), 1), true);
					fluidTank.drain(1, true);
					setEnergy(getEnergy() - ENERGY_USAGE);
				}
				else {
					if(prevEnergy >= getEnergy())
					{
						setActive(false);
					}
				}
			}
			
			prevEnergy = getEnergy();
		}
	}
	
	public boolean isValidGas(GasStack g)
	{
		if(g == null)
		{
			return false;
		}
		
		return g.getGas().hasFluid();
	}
	
	public boolean gasEquals(GasStack gas, FluidStack fluid)
	{
		if(fluid == null || gas == null || !gas.getGas().hasFluid())
		{
			return false;
		}
		
		return gas.getGas().getFluid() == fluid.getFluid();
	}
	
	public boolean isValidFluid(FluidStack f)
	{
		if(f == null)
		{
			return false;
		}
		
		return GasRegistry.getGas(f.getFluid()) != null;
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
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), player);
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		mode = dataStream.readInt();
		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		if(dataStream.readBoolean())
		{
			fluidTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}
		
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(mode);
		data.add(isActive);
		data.add(controlType.ordinal());
		
		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().fluidID);
			data.add(fluidTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
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
        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        
        gasTank.read(nbtTags.getCompoundTag("gasTank"));
        
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
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
    	nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
        
        if(fluidTank.getFluid() != null)
        {
        	nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        }
    }
	
	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}
	
	public int getScaledFluidLevel(int i)
	{
		return fluidTank.getFluid() != null ? fluidTank.getFluid().amount*i / 10000 : 0;
	}
	
	public int getScaledGasLevel(int i)
	{
		return gasTank.getGas() != null ? gasTank.getStored()*i / MAX_GAS : 0;
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(clientActive != active && updateDelay == 0)
    	{
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
    		
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
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return gasTank.receive(stack, true);
	}
	
	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return gasTank.draw(amount, true);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return mode == 1 && side == MekanismUtils.getLeft(facing) ? gasTank.canDraw(type) : false;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return mode == 0 && side == MekanismUtils.getLeft(facing) ? gasTank.canReceive(type) : false;
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
		return mode == 1 && from == MekanismUtils.getRight(facing) && (fluidTank.getFluid() == null ? isValidFluid(new FluidStack(fluid, 1)) : fluidTank.getFluid().getFluid() == fluid);
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
		
		return PipeUtils.EMPTY;
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
	
	@Override
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
		MekanismUtils.saveChunk(this);
	}
}
