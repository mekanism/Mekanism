package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.IRedstoneControl;
import mekanism.common.Object3D;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityGasTank extends TileEntityContainerBlock implements IGasStorage, IGasAcceptor, ITubeConnection, IRedstoneControl
{
	/** The type of gas stored in this tank. */
	public GasStack gasStored;
	
	public final int MAX_GAS = 96000;
	
	/** How fast this tank can output gas. */
	public int output = 16;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	public TileEntityGasTank()
	{
		super("GasTank");
		inventory = new ItemStack[2];
		controlType = RedstoneControl.DISABLED;
	}
	
	@Override
	public void onUpdate()
	{
		if(inventory[0] != null && getGas() != null)
		{
			setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.addGas(inventory[0], getGas())));
		}
		
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
		
		if(!worldObj.isRemote && getGas() != null && MekanismUtils.canFunction(this))
		{
			GasStack toSend = new GasStack(getGas().getGas(), Math.min(getGas().amount, output));
			setGas(new GasStack(getGas().getGas(), getGas().amount - GasTransmission.emitGasToNetwork(toSend, this, ForgeDirection.getOrientation(facing))));
			
			TileEntity tileEntity = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
			
			if(tileEntity instanceof IGasAcceptor)
			{
				if(((IGasAcceptor)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), getGas().getGas()))
				{
					int added = ((IGasAcceptor)tileEntity).receiveGas(new GasStack(getGas().getGas(), Math.min(getGas().amount, output)));
					
					setGas(new GasStack(getGas().getGas(), getGas().amount - added));
				}
			}
		}
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) == null);
		}
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
					((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack));
		}
		
		return false;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IGasItem && (getGas() == null || ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, getGas().getGas()));
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IGasItem && (getGas() == null || ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, getGas().getGas()));
		}
		
		return true;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[] {0} : new int[] {1};
	}

	@Override
	public GasStack getGas(Object... data) 
	{
		return gasStored;
	}

	@Override
	public void setGas(GasStack stack, Object... data) 
	{
		if(stack == null || stack.amount == 0)
		{
			gasStored = null;
		}
		else {
			gasStored = new GasStack(stack.getGas(), Math.max(Math.min(stack.amount, getMaxGas()), 0));
		}
		
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public int getMaxGas(Object... data)
	{
		return MAX_GAS;
	}

	@Override
	public int receiveGas(GasStack stack) 
	{
		if(gasStored == null || (gasStored != null && gasStored.getGas() == stack.getGas()))
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
		return (getGas() == null || getGas().getGas() == type) && side != ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			gasStored = new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt());
		}
		else {
			gasStored = null;
		}
		
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        try {
        	gasStored = GasStack.readFromNBT(nbtTags.getCompoundTag("gasStored"));
        } catch(Exception e) {}
        
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(gasStored != null)
        {
        	nbtTags.setCompoundTag("gasStored", gasStored.write(new NBTTagCompound()));
        }
        
        nbtTags.setInteger("controlType", controlType.ordinal());
    }
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(gasStored != null)
		{
			data.add(true);
			data.add(gasStored.getGas().getID());
			data.add(gasStored.amount);
		}
		else {
			data.add(false);
		}
		
		data.add(controlType.ordinal());
		
		return data;
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side) 
	{
		return true;
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
	}
}
