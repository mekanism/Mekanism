package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

public class TileEntityEnergyCube extends TileEntityElectricBlock implements IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IPeripheral, ICableOutputter, IStrictEnergyAcceptor, IRedstoneControl
{
	/** This Energy Cube's tier. */
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;
	
	/** The redstone level this Energy Cube is outputting at. */
	public int currentRedstoneLevel;
	
	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityEnergyCube()
	{
		super("Energy Cube", 0);
		
		inventory = new ItemStack[2];
		controlType = RedstoneControl.DISABLED;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		ChargeUtils.discharge(1, this);
		
		if(MekanismUtils.canFunction(this))
		{
			CableUtils.emit(this);
		}
	}
	
	@Override
	public double getMaxOutput()
	{
		return tier.OUTPUT;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		
		return true;
	}
	
	@Override
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		HashSet<ForgeDirection> set = new HashSet<ForgeDirection>();
		
		for(ForgeDirection dir : ForgeDirection.values())
		{
			if(dir != ForgeDirection.getOrientation(facing))
			{
				set.add(dir);
			}
		}
		
		return EnumSet.copyOf(set);
	}
	
	@Override
	public ForgeDirection getOutputtingSide()
	{
		return ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return direction != getOutputtingSide();
	}

	@Override
	public int getStored() 
	{
		return (int)(getEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)(getMaxEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return (int)(tier.OUTPUT*Mekanism.TO_IC2);
	}

	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy() - getEnergy())*Mekanism.TO_IC2;
	}
	
	@Override
	public double getOfferedEnergy() 
	{
		return Math.min(getEnergy()*Mekanism.TO_IC2, getOutput());
	}

	@Override
	public void drawEnergy(double amount)
	{
		setEnergy(getEnergy()-amount*Mekanism.FROM_IC2);
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		if(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof TileEntityUniversalCable)
		{
			return i;
		}
		
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
		return side != getOutputtingSide();
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return direction == getOutputtingSide() && !(receiver instanceof TileEntityUniversalCable);
	}

	@Override
	public double getOutputEnergyUnitsPerTick()
	{
		return tier.OUTPUT*Mekanism.TO_IC2;
	}

	@Override
	public double getMaxEnergy() 
	{
		return tier.MAX_ELECTRICITY;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[] {0} : new int[] {1};
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}
		
		return false;
	}

	@Override
	public float getVoltage() 
	{
		return tier.VOLTAGE;
	}

	@Override
	public String getType() 
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {tier.OUTPUT};
			case 2:
				return new Object[] {getMaxEnergy()};
			case 3:
				return new Object[] {(getMaxEnergy()-getEnergy())};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		tier = EnergyCubeTier.getFromName(dataStream.readUTF());
		
		super.handlePacketData(dataStream);
		
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.name);
		
		super.getNetworkedData(data);
		
		data.add(controlType.ordinal());
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setString("tier", tier.name);
        nbtTags.setInteger("controlType", controlType.ordinal());
    }
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
	public void setStored(int energy)
	{
		setEnergy(energy*Mekanism.FROM_IC2);
	}

	@Override
	public int addEnergy(int amount)
	{
		setEnergy(getEnergy() + amount*Mekanism.FROM_IC2);
		return (int)(getEnergy()*Mekanism.TO_IC2);
	}

	@Override
	public boolean isTeleporterCompatible(ForgeDirection side) 
	{
		return side == getOutputtingSide();
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return side == getOutputtingSide();
	}
	
	@Override
	public void setEnergy(double energy) 
	{
	    super.setEnergy(energy);
	    
	    int newRedstoneLevel = getRedstoneLevel();
	    
	    if(newRedstoneLevel != currentRedstoneLevel)
	    {
	        onInventoryChanged();
	        currentRedstoneLevel = newRedstoneLevel;
	    }
	}

	public int getRedstoneLevel()
	{
        double fractionFull = getEnergy()/getMaxEnergy();
        return MathHelper.floor_float((float)(fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
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
