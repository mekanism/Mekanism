package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityChemicalDissolutionChamber extends TileEntityElectricBlock implements IActiveState, ITubeConnection, IRedstoneControl, IHasSound, IGasHandler
{
	public GasTank injectTank = new GasTank(MAX_GAS);
	public GasTank outputTank = new GasTank(MAX_GAS);
	
	public static final int MAX_GAS = 10000;
	
	public static final int INJECT_USAGE = 1;
	
	public int updateDelay;
	
	public int gasOutput = 16;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public double prevEnergy;
	
	public int operatingTicks = 0;
	
	public int TICKS_REQUIRED = 100;
	
	public final double ENERGY_USAGE = Mekanism.rotaryCondensentratorUsage;
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileEntityChemicalDissolutionChamber()
	{
		super("ChemicalDissolutionChamber", MachineType.CHEMICAL_CRYSTALIZER.baseEnergy);
		inventory = new ItemStack[4];
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);
			
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
			
			if(inventory[0] != null && (injectTank.getGas() == null || injectTank.getStored() < injectTank.getMaxGas()))
			{
				injectTank.receive(GasTransmission.removeGas(inventory[0], GasRegistry.getGas("sulfuricAcid"), injectTank.getNeeded()), true);
			}
			
			if(inventory[2] != null && outputTank.getGas() != null)
			{
				outputTank.draw(GasTransmission.addGas(inventory[2], outputTank.getGas()), true);
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
					GasStack stack = RecipeHandler.getItemToGasOutput(inventory[1], true, Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get());
					
					outputTank.receive(stack, true);
					injectTank.draw(INJECT_USAGE, true);
					
					operatingTicks = 0;
					
					if(inventory[1].stackSize <= 0)
					{
						inventory[1] = null;
					}
					
					onInventoryChanged();
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			prevEnergy = getEnergy();
			
			if(outputTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputTank.getGas().getGas(), Math.min(outputTank.getStored(), gasOutput));
				outputTank.draw(GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getRight(facing)), true);
				
				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);
				
				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), outputTank.getGas().getGas()))
					{
						outputTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend), true);
					}
				}
			}
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return RecipeHandler.getItemToGasOutput(itemstack, false, Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get()) != null;
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
		if(slotID == 2)
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
			return new int[] {1};
		}
		else if(side == 0 || side == 1)
		{
			return new int[] {0};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {2};
		}
		
		return InventoryUtils.EMPTY;
	}
	
	public int getScaledProgress(int i)
	{	
		return operatingTicks*i / TICKS_REQUIRED;
	}
	
	public boolean canOperate()
	{
		if(injectTank.getStored() < INJECT_USAGE || inventory[1] == null)
		{
			return false;
		}
		
		GasStack stack = RecipeHandler.getItemToGasOutput(inventory[1], false, Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get());
		
		if(stack == null || (outputTank.getGas() != null && (outputTank.getGas().getGas() != stack.getGas() || outputTank.getNeeded() < stack.amount)))
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
			injectTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			injectTank.setGas(null);
		}
		
		if(dataStream.readBoolean())
		{
			outputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			outputTank.setGas(null);
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
		
		if(injectTank.getGas() != null)
		{
			data.add(true);
			data.add(injectTank.getGas().getGas().getID());
			data.add(injectTank.getStored());
		}
		else {
			data.add(false);
		}
		
		if(outputTank.getGas() != null)
		{
			data.add(true);
			data.add(outputTank.getGas().getGas().getID());
			data.add(outputTank.getStored());
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
        injectTank.read(nbtTags.getCompoundTag("injectTank"));
        outputTank.read(nbtTags.getCompoundTag("gasTank"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
        nbtTags.setInteger("operatingTicks", operatingTicks);
        nbtTags.setCompoundTag("injectTank", injectTank.write(new NBTTagCompound()));
    	nbtTags.setCompoundTag("gasTank", outputTank.write(new NBTTagCompound()));
    }
	
	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}
	
	public int getScaledInjectGasLevel(int i)
	{
		return injectTank.getGas() != null ? injectTank.getStored()*i / MAX_GAS : 0;
	}
	
	public int getScaledOutputGasLevel(int i)
	{
		return outputTank.getGas() != null ? outputTank.getStored()*i / MAX_GAS : 0;
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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
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
	public String getSoundPath()
	{
		return "ChemicalDissolutionChamber.ogg";
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(canReceiveGas(side, stack.getGas()))
		{
			return injectTank.receive(stack, true);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return side == MekanismUtils.getLeft(facing) && type == GasRegistry.getGas("sulfuricAcid");
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}
}
