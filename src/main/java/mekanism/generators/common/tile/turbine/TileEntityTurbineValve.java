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
import mekanism.api.MekanismConfig.general;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.util.CableUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.content.turbine.TurbineFluidTank;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2"),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2")
})
public class TileEntityTurbineValve extends TileEntityTurbineCasing implements IFluidHandler, IEnergyWrapper
{
	public boolean ic2Registered = false;
	
	public TurbineFluidTank fluidTank;

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
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract*general.FROM_TE));

			if(!simulate)
			{
				setEnergy(getEnergy() - toSend);
			}

			return (int)Math.round(toSend*general.TO_TE);
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
		return (int)Math.round(getEnergy()*general.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return (int)Math.round(getMaxEnergy()*general.TO_TE);
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
		return (int)Math.round(getEnergy()*general.TO_IC2);
	}

	@Override
	@Method(modid = "IC2")
	public boolean isTeleporterCompatible(EnumFacing side)
	{
		return canOutputTo(side);
	}

	@Override
	public boolean canOutputTo(EnumFacing side)
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
	public double transferEnergyToAcceptor(EnumFacing side, double amount)
	{
		return 0;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure)) ? new FluidTankInfo[] {fluidTank.getInfo()} : PipeUtils.EMPTY;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		int filled = fluidTank.fill(resource, doFill);
		
		if(structure != null && doFill)
		{
			structure.newSteamInput += filled;
		}
		
		return filled;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(structure != null && structure.fluidStored != null)
		{
			if(resource.getFluid() == structure.fluidStored.getFluid())
			{
				return fluidTank.drain(resource.amount, doDrain);
			}
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(structure != null)
		{
			return fluidTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		if(fluid == FluidRegistry.getFluid("steam"))
		{
			return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
		}
		
		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}
	
	@Override
	public String getName()
	{
		return LangUtils.localize("gui.industrialTurbine");
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}
}
