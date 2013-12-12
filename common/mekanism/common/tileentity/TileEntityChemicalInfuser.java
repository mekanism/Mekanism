package mekanism.common.tileentity;

import mekanism.api.gas.GasStack;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalInfuser extends TileEntityElectricBlock //implements IActiveState, IGasStorage, IGasAcceptor, ITubeConnection, IRedstoneControl
{
	public GasStack leftStack;
	public GasStack rightStack;
	
	public GasStack centerStack;
	
	public static final int MAX_GAS = 10000;
	
	public int updateDelay;
	
	public int gasOutput = 16;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public double prevEnergy;
	
	public final double ENERGY_USAGE = Mekanism.chemicalInfuserUsage;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileEntityChemicalInfuser()
	{
		super("ChemicalInfuser", 0 /*TODO*/);
		inventory = new ItemStack[4];
	}
	
	/*@Override
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
						setGas(GasTransmission.removeGas(inventory[1], null, getMaxGas()));
					}
					else {
						GasStack removed = GasTransmission.removeGas(inventory[1], getGas().getGas(), getMaxGas()-getGas().amount);
						setGas(new GasStack(getGas().getGas(), getGas().amount + (removed != null ? removed.amount : 0)));
					}
				}
				
				if(getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this) && isValidGas(gasTank) && (fluidTank.getFluid() == null || (fluidTank.getFluid().amount < 10000 && gasEquals(gasTank, fluidTank.getFluid()))))
				{
					setActive(true);
					fluidTank.fill(new FluidStack(getGas().getGas().getFluid(), 1), true);
					setGas(new GasStack(getGas().getGas(), getGas().amount-1));
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
				if(getGas() != null)
				{
					if(inventory[0] != null)
					{
						setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.addGas(inventory[0], getGas())));
					}
				}
				
				if(getGas() != null)
				{
					GasStack toSend = new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput));
					setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getLeft(facing))));
					
					TileEntity tileEntity = Object3D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);
					
					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), getGas().getGas()))
						{
							int added = ((IGasHandler)tileEntity).receiveGas(new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput)));
							
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
				
				if(getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this) && isValidFluid(fluidTank.getFluid()) && (gasTank == null || (gasTank.amount < MAX_GAS && gasEquals(gasTank, fluidTank.getFluid()))))
				{
					setActive(true);
					setGas(new GasStack(GasRegistry.getGas(fluidTank.getFluid().getFluid()), getGas() != null ? getGas().amount+1 : 1));
					fluidTank.draw(1, true);
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
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
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
        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        
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
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
        
        if(gasTank != null)
        {
        	nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
        }
        
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
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
		MekanismUtils.saveChunk(this);
	}*/
}
