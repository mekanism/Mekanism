package mekanism.common.tile.prefab;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Method;

public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements IEnergyWrapper
{
	/** How much energy is stored in this block. */
	public double electricityStored;

	/** Maximum amount of energy this machine can hold. */
	public double BASE_MAX_ENERGY;

	/** Actual maximum energy storage, including upgrades */
	public double maxEnergy;

	/**
	 * The base of all blocks that deal with electricity. It has a facing state, initialized state,
	 * and a current amount of stored energy.
	 * @param name - full name of this block
	 * @param baseMaxEnergy - how much energy this block can store
	 */
	public TileEntityElectricBlock(String name, double baseMaxEnergy)
	{
		super(name);
		BASE_MAX_ENERGY = baseMaxEnergy;
		maxEnergy = BASE_MAX_ENERGY;
	}

	@Method(modid = "IC2")
	public void register()
	{
		if(!worldObj.isRemote)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}
	}

	@Method(modid = "IC2")
	public void deregister()
	{
		if(!worldObj.isRemote)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
	}

	@Override
	public void onUpdate()
	{
		/*if(!ic2Registered && MekanismUtils.useIC2())
		{
			register();
		}*/
	}

	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		return EnumSet.noneOf(EnumFacing.class);
	}

	@Override
	public EnumSet<EnumFacing> getConsumingSides()
	{
		return EnumSet.allOf(EnumFacing.class);
	}

	@Override
	public double getMaxOutput()
	{
		return 0;
	}

	@Override
	public double getEnergy()
	{
		return electricityStored;
	}

	@Override
	public void setEnergy(double energy)
	{
		electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
		MekanismUtils.saveChunk(this);
	}

	@Override
	public double getMaxEnergy()
	{
		return maxEnergy;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			setEnergy(dataStream.readDouble());
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		
		data.add(getEnergy());
		
		return data;
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		
		if(MekanismUtils.useIC2())
		{
			register();
		}
	}

	@Override
	public void onChunkUnload()
	{
		if(MekanismUtils.useIC2())
		{
			deregister();
		}

		super.onChunkUnload();
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(MekanismUtils.useIC2())
		{
			deregister();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		electricityStored = nbtTags.getDouble("electricityStored");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setDouble("electricityStored", getEnergy());
		
		return nbtTags;
	}

	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled energy
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(getEnergy()*i / getMaxEnergy());
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, acceptEnergy(from, maxReceive*general.FROM_RF, simulate)*general.TO_RF));
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, pullEnergy(from, maxExtract*general.FROM_RF, simulate)*general.TO_RF));
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return getConsumingSides().contains(from) || getOutputtingSides().contains(from);
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getEnergy()*general.TO_RF));
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getMaxEnergy()*general.TO_RF));
	}

	@Override
	@Method(modid = "IC2")
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	@Method(modid = "IC2")
	public int getSourceTier()
	{
		return 4;
	}

	@Override
	@Method(modid = "IC2")
	public void setStored(int energy)
	{
		setEnergy(energy*general.FROM_IC2);
	}

	@Override
	@Method(modid = "IC2")
	public int addEnergy(int amount)
	{
		setEnergy(getEnergy() + amount*general.FROM_IC2);
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getEnergy()*general.TO_IC2));
	}

	@Override
	@Method(modid = "IC2")
	public boolean isTeleporterCompatible(EnumFacing side)
	{
		return getOutputtingSides().contains(side);
	}

	@Override
	public boolean canOutputEnergy(EnumFacing side)
	{
		return getOutputtingSides().contains(side);
	}

	@Override
	@Method(modid = "IC2")
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction)
	{
		return getConsumingSides().contains(direction);
	}

	@Override
	@Method(modid = "IC2")
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction)
	{
		return getOutputtingSides().contains(direction) && receiver instanceof IEnergyConductor;
	}

	@Override
	@Method(modid = "IC2")
	public int getStored()
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getEnergy()*general.TO_IC2));
	}

	@Override
	@Method(modid = "IC2")
	public int getCapacity()
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getMaxEnergy()*general.TO_IC2));
	}

	@Override
	@Method(modid = "IC2")
	public int getOutput()
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getMaxOutput()*general.TO_IC2));
	}

	@Override
	@Method(modid = "IC2")
	public double getDemandedEnergy()
	{
		return (getMaxEnergy() - getEnergy())*general.TO_IC2;
	}

	@Override
	@Method(modid = "IC2")
	public double getOfferedEnergy()
	{
		return Math.min(getEnergy(), getMaxOutput())*general.TO_IC2;
	}

	@Override
	public boolean canReceiveEnergy(EnumFacing side)
	{
		return getConsumingSides().contains(side);
	}

	@Override
	@Method(modid = "IC2")
	public double getOutputEnergyUnitsPerTick()
	{
		return getMaxOutput()*general.TO_IC2;
	}

	@Override
	@Method(modid = "IC2")
	public double injectEnergy(EnumFacing direction, double amount, double voltage)
	{
		TileEntity tile = getWorld().getTileEntity(getPos().offset(direction));
		
		if(tile == null || CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, direction.getOpposite()))
		{
			return amount;
		}

		return amount-acceptEnergy(direction, amount*general.FROM_IC2, false)*general.TO_IC2;
	}

	@Override
	@Method(modid = "IC2")
	public void drawEnergy(double amount)
	{
		setEnergy(Math.max(getEnergy() - (amount*general.FROM_IC2), 0));
	}

	@Override
	public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(!(getConsumingSides().contains(side) || side == null))
		{
			return 0;
		}

		double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
		
		if(!simulate)
		{
			setEnergy(getEnergy() + toUse);
		}

		return toUse;
	}
	
	@Override
	public double pullEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(!(getOutputtingSides().contains(side) || side == null))
		{
			return 0;
		}
		
		double toGive = Math.min(getEnergy(), amount);
		
		if(!simulate)
		{
			setEnergy(getEnergy() - toGive);
		}
		
		return toGive;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == Capabilities.ENERGY_STORAGE_CAPABILITY
				|| capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY
				|| capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY
				|| capability == Capabilities.TESLA_HOLDER_CAPABILITY
				|| (capability == Capabilities.TESLA_CONSUMER_CAPABILITY && getConsumingSides().contains(facing))
				|| (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && getOutputtingSides().contains(facing))
				|| capability == CapabilityEnergy.ENERGY
				|| super.hasCapability(capability, facing);
	}
	
	private CapabilityWrapperManager teslaManager = new CapabilityWrapperManager(IEnergyWrapper.class, TeslaIntegration.class);
	private CapabilityWrapperManager forgeEnergyManager = new CapabilityWrapperManager(IEnergyWrapper.class, ForgeEnergyIntegration.class);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY ||
				capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY)
		{
			return (T)this;
		}
		
		if(capability == Capabilities.TESLA_HOLDER_CAPABILITY
				|| (capability == Capabilities.TESLA_CONSUMER_CAPABILITY && getConsumingSides().contains(facing))
				|| (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && getOutputtingSides().contains(facing)))
		{
			return (T)teslaManager.getWrapper(this, facing);
		}
		
		if(capability == CapabilityEnergy.ENERGY)
		{
			return (T)forgeEnergyManager.getWrapper(this, facing);
		}
		
		return super.getCapability(capability, facing);
	}
}
