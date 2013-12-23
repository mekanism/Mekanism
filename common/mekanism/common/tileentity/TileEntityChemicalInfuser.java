package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.ChemicalInput;
import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.RecipeHandler;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityChemicalInfuser extends TileEntityElectricBlock implements IActiveState, IGasHandler, ITubeConnection, IRedstoneControl
{
	public GasTank leftTank = new GasTank(MAX_GAS);
	public GasTank rightTank = new GasTank(MAX_GAS);
	public GasTank centerTank = new GasTank(MAX_GAS);
	
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
		super("ChemicalInfuser", MachineType.CHEMICAL_INFUSER.baseEnergy);
		inventory = new ItemStack[4];
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
			
			ChargeUtils.discharge(3, this);
			
			if(inventory[0] != null && (leftTank.getGas() == null || leftTank.getStored() < leftTank.getMaxGas()))
			{
				leftTank.receive(GasTransmission.removeGas(inventory[0], null, leftTank.getNeeded()), true);
			}
			
			if(inventory[2] != null && (rightTank.getGas() == null || rightTank.getStored() < rightTank.getMaxGas()))
			{
				rightTank.receive(GasTransmission.removeGas(inventory[2], null, rightTank.getNeeded()), true);
			}
			
			if(inventory[1] != null && centerTank.getGas() != null)
			{
				centerTank.draw(GasTransmission.addGas(inventory[1], centerTank.getGas()), true);
			}
			
			if(canOperate() && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				GasStack stack = RecipeHandler.getChemicalInfuserOutput(leftTank, rightTank, true);
				
				centerTank.receive(stack, true);
				
				setEnergy(getEnergy() - ENERGY_USAGE);
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			if(centerTank.getGas() != null)
			{
				GasStack toSend = new GasStack(centerTank.getGas().getGas(), Math.min(centerTank.getStored(), gasOutput));
				centerTank.draw(GasTransmission.emitGasToNetwork(toSend, this, ForgeDirection.getOrientation(facing)), true);
				
				TileEntity tileEntity = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
				
				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), centerTank.getGas().getGas()))
					{
						centerTank.draw(((IGasHandler)tileEntity).receiveGas(ForgeDirection.getOrientation(facing).getOpposite(), toSend), true);
					}
				}
			}
			
			prevEnergy = getEnergy();
		}
	}
	
	public boolean canOperate()
	{
		if(leftTank.getGas() == null || rightTank.getGas() == null || centerTank.getNeeded() == 0)
		{
			return false;
		}
		
		GasStack out = RecipeHandler.getChemicalInfuserOutput(leftTank, rightTank, false);
		
		if(out == null)
		{
			return false;
		}
		
		if(centerTank.getNeeded() < out.amount)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				leftTank.setGas(null);
			}
			else if(type == 1)
			{
				rightTank.setGas(null);
			}
			
			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), player);
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		if(dataStream.readBoolean())
		{
			leftTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			leftTank.setGas(null);
		}
		
		if(dataStream.readBoolean())
		{
			rightTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			rightTank.setGas(null);
		}
		
		if(dataStream.readBoolean())
		{
			centerTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			centerTank.setGas(null);
		}
		
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(isActive);
		data.add(controlType.ordinal());
		
		if(leftTank != null)
		{
			data.add(true);
			data.add(leftTank.getGas().getGas().getID());
			data.add(leftTank.getStored());
		}
		else {
			data.add(false);
		}
		
		if(rightTank != null)
		{
			data.add(true);
			data.add(rightTank.getGas().getGas().getID());
			data.add(rightTank.getStored());
		}
		else {
			data.add(false);
		}
		
		if(centerTank != null)
		{
			data.add(true);
			data.add(centerTank.getGas().getGas().getID());
			data.add(centerTank.getStored());
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
        
        leftTank.read(nbtTags.getCompoundTag("leftTank"));
        rightTank.read(nbtTags.getCompoundTag("rightTank"));
        centerTank.read(nbtTags.getCompoundTag("centerTank"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
        
    	nbtTags.setCompoundTag("leftTank", leftTank.write(new NBTTagCompound()));
      	nbtTags.setCompoundTag("rightTank", rightTank.write(new NBTTagCompound()));
      	nbtTags.setCompoundTag("centerTank", centerTank.write(new NBTTagCompound()));
    }
	
	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}
	
	public GasTank getTank(ForgeDirection side)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank;
		}
		else if(side == ForgeDirection.getOrientation(facing))
		{
			return centerTank;
		}
		
		return null;
	}
	
	public int getScaledLeftGasLevel(int i)
	{
		return leftTank != null ? leftTank.getStored()*i / MAX_GAS : 0;
	}
	
	public int getScaledRightGasLevel(int i)
	{
		return leftTank != null ? rightTank.getStored()*i / MAX_GAS : 0;
	}
	
	public int getScaledCenterGasLevel(int i)
	{
		return leftTank != null ? centerTank.getStored()*i / MAX_GAS : 0;
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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing) || side == ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return getTank(side) != null && getTank(side) != centerTank ? getTank(side).canReceive(type) : false;
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

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(canReceiveGas(side, stack != null ? stack.getGas() : null))
		{
			return getTank(side).receive(stack, true);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		if(canDrawGas(side, null))
		{
			return getTank(side).draw(amount, true);
		}
		
		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return getTank(side) != null && getTank(side) == centerTank ? getTank(side).canDraw(type) : false;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0 || slotID == 1)
		{
			return RecipeHandler.getChemicalFormulatorOutput(itemstack, false) != null;
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return false;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 0 || slotID == 2)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, null);
		}
		if(slotID == 1)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
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
		else if(side == facing)
		{
			return new int[] {1};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {2};
		}
		else if(side == 0 || side == 1)
		{
			return new int[3];
		}
		
		return InventoryUtils.EMPTY;
	}
}
