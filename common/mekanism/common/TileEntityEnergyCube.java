package mekanism.common;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import mekanism.api.ICableOutputter;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.Object3D;
import mekanism.common.Tier.EnergyCubeTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityEnergyCube extends TileEntityElectricBlock implements IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IPeripheral, ICableOutputter, IStrictEnergyAcceptor
{
	/** This Energy Cube's tier. */
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;
	
	/** The redstone level this Energy Cube is outputting at. */
	public int currentRedstoneLevel;
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityEnergyCube()
	{
		super("Energy Cube", 0);
		
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		ChargeUtils.charge(0, this);
		ChargeUtils.discharge(1, this);
		
		if(!worldObj.isRemote)
		{
			TileEntity tileEntity = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
			
			if(electricityStored > 0)
			{
				if(tileEntity instanceof IUniversalCable)
				{
					setEnergy(electricityStored - (Math.min(electricityStored, tier.OUTPUT) - CableUtils.emitEnergyToNetwork(Math.min(electricityStored, tier.OUTPUT), this, ForgeDirection.getOrientation(facing))));
					return;
				}
				else if((tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergyAcceptor) && Mekanism.hooks.IC2Loaded)
				{
					if(electricityStored >= tier.OUTPUT)
					{
						EnergyTileSourceEvent event = new EnergyTileSourceEvent(this, (int)(tier.OUTPUT*Mekanism.TO_IC2));
						MinecraftForge.EVENT_BUS.post(event);
						setEnergy(electricityStored - (tier.OUTPUT - (event.amount*Mekanism.FROM_IC2)));
					}
				}
				else if(tileEntity instanceof IPowerReceptor && Mekanism.hooks.BuildCraftLoaded)
				{
					PowerReceiver receiver = ((IPowerReceptor)tileEntity).getPowerReceiver(ForgeDirection.getOrientation(facing).getOpposite());
	            	double electricityNeeded = Math.min(receiver.powerRequest(), receiver.getMaxEnergyStored() - receiver.getEnergyStored())*Mekanism.FROM_BC;
	            	double transferEnergy = Math.min(electricityStored, Math.min(electricityNeeded, tier.OUTPUT));
	            	receiver.receiveEnergy(Type.STORAGE, (float)(transferEnergy*Mekanism.TO_BC), ForgeDirection.getOrientation(facing).getOpposite());
	            	setEnergy(electricityStored - transferEnergy);
				}
			}
			
			if(tileEntity instanceof IConductor)
			{
				ForgeDirection outputDirection = ForgeDirection.getOrientation(facing);
				float provide = getProvide(outputDirection);
	
				if(provide > 0)
				{
					IElectricityNetwork outputNetwork = ElectricityHelper.getNetworkFromTileEntity(tileEntity, outputDirection);
		
					if(outputNetwork != null)
					{
						ElectricityPack request = outputNetwork.getRequest(this);
						
						if(request.getWatts() > 0)
						{
							ElectricityPack sendPack = ElectricityPack.min(ElectricityPack.getFromWatts(getEnergyStored(), getVoltage()), ElectricityPack.getFromWatts(provide, getVoltage()));
							float rejectedPower = outputNetwork.produce(sendPack, this);
							setEnergyStored(getEnergyStored() - (sendPack.getWatts() - rejectedPower));
						}
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
			return MekanismUtils.canBeCharged(itemstack);
		}
		else if(slotID == 1)
		{
			return MekanismUtils.canBeDischarged(itemstack);
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
	protected EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public float getProvide(ForgeDirection direction)
	{
		return getOutputtingSides().contains(direction) ? (float)Math.min(getMaxEnergy()-getEnergy(), tier.OUTPUT) : 0;
	}

	@Override
	public int getStored() 
	{
		return (int)(electricityStored*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)(tier.MAX_ELECTRICITY*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return (int)(tier.OUTPUT*Mekanism.TO_IC2);
	}

	@Override
	public int demandsEnergy() 
	{
		return (int)((tier.MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = tier.MAX_ELECTRICITY-electricityStored;
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
    }
	
	@Override
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = tier.MAX_ELECTRICITY-electricityStored;
    	
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
		return side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return (int)(tier.OUTPUT*Mekanism.TO_IC2);
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
			return MekanismUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return MekanismUtils.canBeOutputted(itemstack, true);
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
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {tier.OUTPUT};
			case 2:
				return new Object[] {tier.MAX_ELECTRICITY};
			case 3:
				return new Object[] {(tier.MAX_ELECTRICITY-electricityStored)};
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
		super.handlePacketData(dataStream);
		tier = EnergyCubeTier.getFromName(dataStream.readUTF());
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(tier.name);
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setString("tier", tier.name);
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
		setEnergy(electricityStored + amount*Mekanism.FROM_IC2);
		return (int)electricityStored;
	}

	@Override
	public boolean isTeleporterCompatible(Direction side) 
	{
		return true;
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return side == ForgeDirection.getOrientation(facing);
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
        return MathHelper.floor_float((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
	}
}
