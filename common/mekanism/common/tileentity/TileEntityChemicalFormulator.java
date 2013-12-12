package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.RecipeHandler;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityChemicalFormulator extends TileEntityElectricBlock implements IActiveState, IGasStorage, ITubeConnection, IRedstoneControl
{
	public GasStack gasTank;
	
	public static final int MAX_GAS = 10000;
	
	public int updateDelay;
	
	public int gasOutput = 16;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public double prevEnergy;
	
	public int operatingTicks = 0;
	
	public int TICKS_REQUIRED = 100;
	
	public final double ENERGY_USAGE = Mekanism.rotaryCondensentratorUsage;
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileEntityChemicalFormulator()
	{
		super("ChemicalFormulator", 0 /*TODO*/);
		inventory = new ItemStack[3];
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
			
			ChargeUtils.discharge(1, this);
			
			if(getGas() != null)
			{
				if(inventory[2] != null)
				{
					setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.addGas(inventory[2], getGas())));
				}
			}
			
			if(canOperate() && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - ENERGY_USAGE);
				
				if(operatingTicks < TICKS_REQUIRED)
				{
					operatingTicks++;
				}
				else {
					GasStack stack = RecipeHandler.getChemicalFormulatorOutput(inventory[0], true);
					
					setGas(new GasStack(stack.getGas(), getStoredGas()+stack.amount));
					operatingTicks = 0;
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			prevEnergy = getEnergy();
			
			if(getGas() != null)
			{
				GasStack toSend = new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput));
				setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getLeft(facing))));
				
				TileEntity tileEntity = Object3D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);
				
				if(tileEntity instanceof IGasAcceptor)
				{
					if(((IGasAcceptor)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), getGas().getGas()))
					{
						int added = ((IGasAcceptor)tileEntity).receiveGas(new GasStack(getGas().getGas(), Math.min(getGas().amount, gasOutput)));
						
						setGas(new GasStack(getGas().getGas(), getGas().amount - added));
					}
				}
			}
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return RecipeHandler.getChemicalFormulatorOutput(itemstack, false) != null;
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return false;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return inventory[2] != null && inventory[2].getItem() instanceof IGasItem && ((IGasItem)inventory[2].getItem()).canProvideGas(inventory[2], null);
		}
		
		return false;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == MekanismUtils.getLeft(facing).ordinal())
		{
			return new int[] {0};
		}
		else if(side == 0 || side == 1)
		{
			return new int[] {1};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {2};
		}
		
		return new int[0];
	}
	
	public int getStoredGas()
	{
		return gasTank != null ? gasTank.amount : 0;
	}
	
	public int getScaledProgress(int i)
	{	
		return operatingTicks*i / TICKS_REQUIRED;
	}
	
	public boolean canOperate()
	{
		if(inventory[0] == null)
		{
			return false;
		}
		
		GasStack stack = RecipeHandler.getChemicalFormulatorOutput(inventory[0], false);
		
		if(stack == null || (getGas() != null && (getGas().getGas() != stack.getGas() || getMaxGas()-getGas().amount < stack.amount)))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		operatingTicks = dataStream.readInt();
		
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
		
		data.add(isActive);
		data.add(controlType.ordinal());
		data.add(operatingTicks);
		
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

        isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
        operatingTicks = nbtTags.getInteger("operatingTicks");
        
        gasTank = GasStack.readFromNBT(nbtTags.getCompoundTag("gasTank"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
        nbtTags.setInteger("operatingTicks", operatingTicks);
        
        if(gasTank != null)
        {
        	nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
        }
    }
	
	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
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
		return side == MekanismUtils.getRight(facing);
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
	}
}
