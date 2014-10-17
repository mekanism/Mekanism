package mekanism.common.tile;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tile.IEnergyStorage;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.Upgrade;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;


@InterfaceList({
		@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
		@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2"),
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHAPI|energy"),
		@Interface(iface = "buildcraft.api.power.IPowerReceptor", modid = "BuildCraftAPI|power"),
		@Interface(iface = "buildcraft.api.power.IPowerEmitter", modid = "BuildCraftAPI|power")
})
public abstract class TileEntityElectricBlock extends TileEntityContainerBlock implements ITileNetwork, IPowerEmitter, IPowerReceptor, IStrictEnergyStorage, IEnergyHandler, IEnergySink, IEnergySource, IEnergyStorage, IStrictEnergyAcceptor, ICableOutputter
{
	/** How much energy is stored in this block. */
	public double electricityStored;

	/** Maximum amount of energy this machine can hold. */
	public double BASE_MAX_ENERGY;

	/** Actual maximum energy storage, including upgrades */
	public double maxEnergy;

	/** BuildCraft power handler. */
	public PowerHandler powerHandler;

	/** Is this registered with IC2 */
	public boolean ic2Registered = false;

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

		if(MekanismUtils.useBuildCraft())
		{
			configure();
		}
	}

	@Method(modid = "BuildCraftAPI|power")
	public void configure()
	{
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configurePowerPerdition(0, 0);
		powerHandler.configure(0, 0, 0, 0);
	}


	@Method(modid = "IC2")
	public void register()
	{
		if(!worldObj.isRemote)
		{
			TileEntity registered = EnergyNet.instance.getTileEntity(worldObj, xCoord, yCoord, zCoord);
			
			if(registered != this)
			{
				if(registered instanceof IEnergyTile)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)registered));
				}
				else if(registered == null)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
					ic2Registered = true;
				}
			}
		}
	}

	@Method(modid = "IC2")
	public void deregister()
	{
		if(!worldObj.isRemote)
		{
			TileEntity registered = EnergyNet.instance.getTileEntity(worldObj, xCoord, yCoord, zCoord);
			
			if(registered instanceof IEnergyTile)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)registered));
			}
		}
	}

	@Override
	public void onUpdate()
	{
		if(MekanismUtils.useBuildCraft())
		{
			reconfigure();
		}
		
		if(!ic2Registered && MekanismUtils.useIC2())
		{
			register();
		}
	}

	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.remove(ForgeDirection.UNKNOWN);
		return set;
	}

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
		setEnergy(dataStream.readDouble());
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
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

		if(MekanismUtils.useBuildCraft())
		{
			reconfigure();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setDouble("electricityStored", getEnergy());
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		if(this instanceof IUpgradeTile && upgrade == Upgrade.ENERGY)
		{
			maxEnergy = MekanismUtils.getMaxEnergy(((IUpgradeTile)this), BASE_MAX_ENERGY);
		}
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if(getConsumingSides().contains(side))
		{
			return powerHandler.getPowerReceiver();
		}

		return null;
	}

	@Method(modid = "BuildCraftAPI|power")
	protected void reconfigure()
	{
		powerHandler.configure(0, (float)((getMaxEnergy()-getEnergy())*general.TO_BC), 0, (float)(getMaxEnergy()*general.TO_BC));
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	public void doWork(PowerHandler workProvider)
	{
		if(powerHandler.getEnergyStored() > 0)
		{
			if(getEnergy() < getMaxEnergy())
			{
				setEnergy(getEnergy() + powerHandler.useEnergy(0, (float)((getMaxEnergy()-getEnergy())* general.TO_BC), true)* general.FROM_BC);
			}

			powerHandler.setEnergy(0);
		}

		reconfigure();
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	public World getWorld()
	{
		return worldObj;
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
	@Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(getConsumingSides().contains(from))
		{
			double toAdd = (int)Math.min(getMaxEnergy()-getEnergy(), maxReceive* general.FROM_TE);

			if(!simulate)
			{
				setEnergy(getEnergy() + toAdd);
			}

			return (int)Math.round(toAdd* general.TO_TE);
		}

		return 0;
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if(getOutputtingSides().contains(from))
		{
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract* general.FROM_TE));

			if(!simulate)
			{
				setEnergy(getEnergy() - toSend);
			}

			return (int)Math.round(toSend* general.TO_TE);
		}

		return 0;
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return getConsumingSides().contains(from) || getOutputtingSides().contains(from);
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getEnergy()* general.TO_TE);
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getMaxEnergy()* general.TO_TE);
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
		return (int)Math.round(getEnergy()*general.TO_IC2);
	}

	@Override
	@Method(modid = "IC2")
	public boolean isTeleporterCompatible(ForgeDirection side)
	{
		return getOutputtingSides().contains(side);
	}

	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return getOutputtingSides().contains(side);
	}

	@Override
	@Method(modid = "IC2")
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return getConsumingSides().contains(direction);
	}

	@Override
	@Method(modid = "IC2")
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return getOutputtingSides().contains(direction) && receiver instanceof IEnergyConductor;
	}

	@Override
	@Method(modid = "IC2")
	public int getStored()
	{
		return (int)Math.round(getEnergy()*general.TO_IC2);
	}

	@Override
	@Method(modid = "IC2")
	public int getCapacity()
	{
		return (int)Math.round(getMaxEnergy()*general.TO_IC2);
	}

	@Override
	@Method(modid = "IC2")
	public int getOutput()
	{
		return (int)Math.round(getMaxOutput()*general.TO_IC2);
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
	public boolean canReceiveEnergy(ForgeDirection side)
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
	public double injectEnergy(ForgeDirection direction, double amount, double voltage)
	{
		if(Coord4D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof IGridTransmitter)
		{
			return amount;
		}

		return amount-transferEnergyToAcceptor(direction, amount*general.FROM_IC2)*general.TO_IC2;
	}

	@Override
	@Method(modid = "IC2")
	public void drawEnergy(double amount)
	{
		setEnergy(Math.max(getEnergy() - (amount*general.FROM_IC2), 0));
	}

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!(getConsumingSides().contains(side) || side == ForgeDirection.UNKNOWN))
		{
			return 0;
		}

		double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
		setEnergy(getEnergy() + toUse);

		return toUse;
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	public boolean canEmitPowerFrom(ForgeDirection side)
	{
		return getOutputtingSides().contains(side);
	}
}
