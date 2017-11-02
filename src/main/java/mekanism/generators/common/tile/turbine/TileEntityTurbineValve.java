package mekanism.generators.common.tile.turbine;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;

import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.CableUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.content.turbine.TurbineFluidTank;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional.Method;

public class TileEntityTurbineValve extends TileEntityTurbineCasing implements IFluidHandlerWrapper, IEnergyWrapper, IComputerIntegration
{
	public boolean ic2Registered = false;
	
	public TurbineFluidTank fluidTank;
	
	public Fluid STEAM = FluidRegistry.getFluid("steam");

	public TileEntityTurbineValve()
	{
		super("TurbineValve");
		fluidTank = new TurbineFluidTank(this);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!ic2Registered && MekanismUtils.useIC2())
		{
			register();
		}
		
		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				double prev = getEnergy();
				CableUtils.emit(this);
			}
		}
	}
	
	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		if(structure != null)
		{
			EnumSet set = EnumSet.allOf(EnumFacing.class);
			
			for(EnumFacing side : EnumFacing.VALUES)
			{
				if(structure.locations.contains(Coord4D.get(this).offset(side)))
				{
					set.remove(side);
				}
			}
			
			return set;
		}
		
		return EnumSet.noneOf(EnumFacing.class);
	}

	@Override
	public EnumSet<EnumFacing> getConsumingSides()
	{
		return EnumSet.noneOf(EnumFacing.class);
	}
	
	@Method(modid = "IC2")
	public void register()
	{
		if(!worldObj.isRemote)
		{
			IEnergyTile registered = EnergyNet.instance.getTile(worldObj, getPos());
			
			if(registered != this)
			{
				if(registered instanceof IEnergyTile)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
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
			IEnergyTile registered = EnergyNet.instance.getTile(worldObj, getPos());
			
			if(registered instanceof IEnergyTile)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
			}
		}
	}

	@Override
	public double getMaxOutput()
	{
		return structure != null ? structure.getEnergyCapacity() : 0;
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
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		return 0;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		if(getOutputtingSides().contains(from))
		{
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract*general.FROM_RF));

			if(!simulate)
			{
				setEnergy(getEnergy() - toSend);
			}

			return (int)Math.round(Math.min(Integer.MAX_VALUE, toSend*general.TO_RF));
		}

		return 0;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return structure != null;
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return structure != null ? (int)Math.round(Math.min(Integer.MAX_VALUE, getEnergy()*general.TO_RF)) : 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return structure != null ? (int)Math.round(Math.min(Integer.MAX_VALUE, getMaxEnergy()*general.TO_RF)) : 0;
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
		return 0;
	}

	@Override
	@Method(modid = "IC2")
	public boolean isTeleporterCompatible(EnumFacing side)
	{
		return canOutputEnergy(side);
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
		return false;
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
		return 0;
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
		return false;
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
		return amount;
	}

	@Override
	@Method(modid = "IC2")
	public void drawEnergy(double amount)
	{
		if(structure != null)
		{
			double toDraw = Math.min(amount*general.FROM_IC2, getMaxOutput());
			setEnergy(Math.max(getEnergy() - toDraw, 0));
		}
	}

	@Override
	public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
	{
		return 0;
	}
	
	@Override
	public double pullEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(!getOutputtingSides().contains(side))
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
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure)) ? new FluidTankInfo[] {fluidTank.getInfo()} : PipeUtils.EMPTY;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(!canFill(from, resource != null ? resource.getFluid() : null))
		{
			return 0;
		}
		
		int filled = fluidTank.fill(resource, doFill);
		
		if(doFill)
		{
			structure.newSteamInput += filled;
		}
		
		if(filled < structure.getFluidCapacity() && structure.dumpMode != GasMode.IDLE)
		{
			filled = structure.getFluidCapacity();
		}
		
		return filled;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		if(fluid == STEAM)
		{
			return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
		}
		
		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return false;
	}
	
	@Override
	public String getName()
	{
		return LangUtils.localize("gui.industrialTurbine");
	}
	
	private static final String[] methods = new String[] {"isFormed", "getSteam", "getFlowRate", "getMaxFlow", "getSteamInput"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		if(method == 0)
		{
			return new Object[] {structure != null};
		}
		else {
			if(structure == null)
			{
				return new Object[] {"Unformed"};
			}
			
			switch(method)
			{
				case 1:
					return new Object[] {structure.fluidStored != null ? structure.fluidStored.amount : 0};
				case 2:
					return new Object[] {structure.clientFlow};
				case 3:
					double rate = structure.lowerVolume*(structure.clientDispersers*generators.turbineDisperserGasFlow);		
					rate = Math.min(rate, structure.vents*generators.turbineVentGasFlow);
					return new Object[] {rate};
				case 4:
					return new Object[] {structure.lastSteamInput};
			}
		}
		
		throw new NoSuchMethodException();
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
					|| capability == Capabilities.ENERGY_STORAGE_CAPABILITY
					|| capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY
					|| capability == Capabilities.TESLA_HOLDER_CAPABILITY
					|| (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && getOutputtingSides().contains(facing))
					|| capability == CapabilityEnergy.ENERGY)
			{
				return true;
			}
		}
		
		return super.hasCapability(capability, side);
	}
	
	private CapabilityWrapperManager teslaManager = new CapabilityWrapperManager(IEnergyWrapper.class, TeslaIntegration.class);
	private CapabilityWrapperManager forgeEnergyManager = new CapabilityWrapperManager(IEnergyWrapper.class, ForgeEnergyIntegration.class);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			if(capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY)
			{
				return (T)this;
			}

			if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			{
				return (T)new FluidHandlerWrapper(this, side);
			}
			
			if(capability == Capabilities.TESLA_HOLDER_CAPABILITY
					|| (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && getOutputtingSides().contains(facing)))
			{
				return (T)teslaManager.getWrapper(this, facing);
			}
			
			if(capability == CapabilityEnergy.ENERGY)
			{
				return (T)forgeEnergyManager.getWrapper(this, facing);
			}
		}
		
		return super.getCapability(capability, side);
	}
}
