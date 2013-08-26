package mekanism.generators.common.tileentity;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Object3D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public abstract class TileEntityGenerator extends TileEntityElectricBlock implements IEnergySource, IEnergyStorage, IPowerReceptor, IPeripheral, IActiveState, IHasSound, ICableOutputter, IRedstoneControl
{
	/** Output per tick this generator can transfer. */
	public double output;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The client's current active state. */
	public boolean clientActive;
	
	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType;
	
	/**
	 * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
	 * @param name - full name of this generator
	 * @param maxEnergy - how much energy this generator can store
	 * @param maxFuel - how much fuel this generator can store
	 */
	public TileEntityGenerator(String name, double maxEnergy, double out)
	{
		super(name, maxEnergy);
		
		powerHandler.configure(0, 0, 0, (int)(maxEnergy*Mekanism.TO_BC));
		
		output = out;
		isActive = false;
		controlType = RedstoneControl.DISABLED;
	}
	
	@Override
	public void onUpdate()
	{	
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);
		}
			
		if(!worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;
				
				if(updateDelay == 0 && clientActive != isActive)
				{
					clientActive = isActive;
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
				}
			}
			
			TileEntity tileEntity = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
			
			if(getEnergy() > 0)
			{
				if(!worldObj.isRemote)
				{
					if(TransmissionType.checkTransmissionType(tileEntity, TransmissionType.ENERGY))
					{
						setEnergy(getEnergy() - (Math.min(getEnergy(), output) - CableUtils.emitEnergyToNetwork(Math.min(getEnergy(), output), this, ForgeDirection.getOrientation(facing))));
						return;
					}
					else if(tileEntity instanceof IPowerReceptor && Mekanism.hooks.BuildCraftLoaded)
					{
						PowerReceiver receiver = ((IPowerReceptor)tileEntity).getPowerReceiver(ForgeDirection.getOrientation(facing).getOpposite());
						if(receiver != null)
						{
			            	double electricityNeeded = Math.min(receiver.powerRequest(), receiver.getMaxEnergyStored() - receiver.getEnergyStored())*Mekanism.FROM_BC;
			            	double transferEnergy = Math.min(getEnergy(), Math.min(electricityNeeded, output));
			            	receiver.receiveEnergy(Type.STORAGE, (float)(transferEnergy*Mekanism.TO_BC), ForgeDirection.getOrientation(facing).getOpposite());
			            	setEnergy(getEnergy() - transferEnergy);
						}
					}
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
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}
	
	@Override
	protected EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}
	
	@Override
	public float getRequest(ForgeDirection direction)
	{
		return 0;
	}
	
	@Override
	public float getProvide(ForgeDirection direction)
	{
		return getOutputtingSides().contains(direction) ? Math.min(getEnergyStored(), (float)(output*Mekanism.TO_UE)) : 0;
	}
	
	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide) 
	{
		if(getOutputtingSides().contains(from))
		{
			double toSend = Math.min(getEnergy(), Math.min(output, request.getWatts()*Mekanism.FROM_UE));
			
			if(doProvide)
			{
				setEnergy(getEnergy() - toSend);
			}
			
			return ElectricityPack.getFromWatts((float)(toSend*Mekanism.TO_UE), getVoltage());
		}
		
		return new ElectricityPack();
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.unregisterSound(this);
		}
	}
	
	/**
	 * Gets the boost this generator can receive in it's current location.
	 * @return environmental boost
	 */
	public abstract double getEnvironmentBoost();
	
	/**
	 * Whether or not this generator can operate.
	 * @return if the generator can operate
	 */
	public abstract boolean canOperate();
	
	@Override
	public boolean getActive()
	{
		return isActive;
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
	public String getType() 
	{
		return getInvName();
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
	public double getOutputEnergyUnitsPerTick()
	{
		return output*Mekanism.TO_IC2;
	}
	
	@Override
	public void setFacing(short orientation)
	{
		super.setFacing(orientation);
		
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, MekanismGenerators.generatorID);
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}
	
	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return direction == ForgeDirection.getOrientation(facing);
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
		return (int)(output*Mekanism.TO_IC2);
	}
	
	@Override
	public boolean isTeleporterCompatible(ForgeDirection side) 
	{
		return side == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public int addEnergy(int amount)
	{
		return (int)(getEnergy()*Mekanism.TO_IC2);
	}
	
	@Override
	public void setStored(int energy)
	{
		setEnergy(energy*Mekanism.FROM_IC2);
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
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(isActive);
		data.add(controlType.ordinal());
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        clientActive = isActive = nbtTags.getBoolean("isActive");
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("controlType", controlType.ordinal());
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return side == ForgeDirection.getOrientation(facing);
	}
	
	@Override
	public String getSoundPath()
	{
		return fullName.replace(" ", "").replace("-","").replace("Advanced", "") + ".ogg";
	}
	
	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}
	
	@Override
	public boolean hasVisual()
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
