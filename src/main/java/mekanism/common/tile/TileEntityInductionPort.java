package mekanism.common.tile;

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

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2"),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2")
})
public class TileEntityInductionPort extends TileEntityInductionCasing implements IEnergyWrapper, IConfigurable, IActiveState
{
	/** false = input, true = output */
	public boolean mode;
	
	public TileEntityInductionPort()
	{
		super("InductionPort");
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		/*if(!ic2Registered && MekanismUtils.useIC2())
		{
			register();
		}*/
		
		if(!worldObj.isRemote)
		{
			if(structure != null && mode)
			{
				double prev = getEnergy();
				CableUtils.emit(this);
				structure.remainingOutput -= (prev-getEnergy());
			}
		}
	}
	
	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		if(structure != null && mode)
		{
			EnumSet<EnumFacing> set = EnumSet.allOf(EnumFacing.class);
			
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
		if(structure != null && !mode)
		{
			return EnumSet.allOf(EnumFacing.class);
		}
		
		return EnumSet.noneOf(EnumFacing.class);
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
	public double getMaxOutput()
	{
		return structure != null ? structure.remainingOutput : 0;
	}
	
	private double getMaxInput()
	{
		return structure != null ? structure.remainingInput : 0;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			boolean prevMode = mode;
			mode = dataStream.readBoolean();
			
			if(prevMode != mode)
			{
				MekanismUtils.updateBlock(worldObj, getPos());
			}
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		
		data.add(mode);
		
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

		mode = nbtTags.getBoolean("mode");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("mode", mode);
		
		return nbtTags;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		if(getConsumingSides().contains(from))
		{
			double toAdd = (int)Math.min(Math.min(getMaxInput(), getMaxEnergy()-getEnergy()), maxReceive* general.FROM_RF);

			if(!simulate)
			{
				setEnergy(getEnergy() + toAdd);
				structure.remainingInput -= toAdd;
			}

			return (int)Math.round(Math.min(Integer.MAX_VALUE, toAdd*general.TO_RF));
		}

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
				structure.remainingOutput -= toSend;
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
		double toUse = Math.min(Math.min(getMaxInput(), getMaxEnergy()-getEnergy()), amount*general.FROM_IC2);
		setEnergy(getEnergy() + toUse);
		structure.remainingInput -= toUse;
		return (int)Math.round(Math.min(Integer.MAX_VALUE, getEnergy()*general.TO_IC2));
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
		if(structure != null)
		{
			double toDraw = Math.min(amount*general.FROM_IC2, getMaxOutput());
			setEnergy(Math.max(getEnergy() - toDraw, 0));
			structure.remainingOutput -= toDraw;
		}
	}

	@Override
	public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(!getConsumingSides().contains(side))
		{
			return 0;
		}

		double toUse = Math.min(Math.min(getMaxInput(), getMaxEnergy()-getEnergy()), amount);
		
		if(!simulate)
		{
			setEnergy(getEnergy() + toUse);
			structure.remainingInput -= toUse;
		}

		return toUse;
	}
	
	@Override
	public double pullEnergy(EnumFacing side, double amount, boolean simulate)
	{
		if(!getOutputtingSides().contains(side))
		{
			return 0;
		}
		
		double toGive = Math.min(getEnergy(), amount);
		setEnergy(getEnergy() - toGive);
		
		return toGive;
	}

	@Override
	public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side)
	{
		if(!worldObj.isRemote)
		{
			mode = !mode;
			String modeText = " " + (mode ? EnumColor.DARK_RED : EnumColor.DARK_GREEN) + LangUtils.transOutputInput(mode) + ".";
			player.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("tooltip.configurator.inductionPortMode") + modeText));
			
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));
			markDirty();
		}
		
		return EnumActionResult.SUCCESS;
	}

	@Override
	public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side)
	{
		return EnumActionResult.PASS;
	}

	@Override
	public boolean getActive()
	{
		return mode;
	}

	@Override
	public void setActive(boolean active)
	{
		mode = active;
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == Capabilities.ENERGY_STORAGE_CAPABILITY
				|| capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY
				|| capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY
				|| capability == Capabilities.TESLA_HOLDER_CAPABILITY
				|| capability == Capabilities.CONFIGURABLE_CAPABILITY
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
				capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY || capability == Capabilities.CONFIGURABLE_CAPABILITY)
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