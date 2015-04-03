package mekanism.common.tile;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyTile;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Range4D;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.CableUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2"),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2"),
	@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHCore"),
})
public class TileEntityInductionPort extends TileEntityInductionCasing implements IEnergyWrapper, IConfigurable, IActiveState
{
	public boolean ic2Registered = false;
	
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
		
		if(!ic2Registered && MekanismUtils.useIC2())
		{
			register();
		}
		
		if(!worldObj.isRemote)
		{
			if(structure != null && mode == true)
			{
				double prev = getEnergy();
				CableUtils.emit(this);
				structure.remainingOutput -= (prev-getEnergy());
			}
		}
	}
	
	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		if(structure != null && mode == true)
		{
			EnumSet set = EnumSet.allOf(ForgeDirection.class);
			set.remove(ForgeDirection.UNKNOWN);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(structure.locations.contains(Coord4D.get(this).getFromSide(side)))
				{
					set.remove(side);
				}
			}
			
			return set;
		}
		
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		if(structure != null && mode == false)
		{
			EnumSet set = EnumSet.allOf(ForgeDirection.class);
			set.remove(ForgeDirection.UNKNOWN);
			return set;
		}
		
		return EnumSet.noneOf(ForgeDirection.class);
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
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
	public double getMaxOutput()
	{
		return structure != null ? structure.remainingOutput : 0;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		mode = dataStream.readBoolean();
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
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
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("mode", mode);
	}

	@Override
	@Method(modid = "CoFHCore")
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
	@Method(modid = "CoFHCore")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		if(getOutputtingSides().contains(from))
		{
			double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract*general.FROM_TE));

			if(!simulate)
			{
				setEnergy(getEnergy() - toSend);
				structure.remainingOutput -= toSend;
			}

			return (int)Math.round(toSend* general.TO_TE);
		}

		return 0;
	}

	@Override
	@Method(modid = "CoFHCore")
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return structure != null;
	}

	@Override
	@Method(modid = "CoFHCore")
	public int getEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getEnergy()* general.TO_TE);
	}

	@Override
	@Method(modid = "CoFHCore")
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
		return canOutputTo(side);
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
		if(Coord4D.get(this).getFromSide(direction).getTileEntity(worldObj) instanceof ITransmitterTile)
		{
			return amount;
		}

		return amount-transferEnergyToAcceptor(direction, amount*general.FROM_IC2)*general.TO_IC2;
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
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!getConsumingSides().contains(side))
		{
			return 0;
		}

		double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
		setEnergy(getEnergy() + toUse);

		return toUse;
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side) 
	{
		if(!worldObj.isRemote)
		{
			mode = !mode;
			String modeText = " " + (mode ? EnumColor.DARK_RED : EnumColor.DARK_GREEN) + LangUtils.transOutputInput(mode) + ".";
			player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.configurator.inductionPortMode") + modeText));
			
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			markDirty();
		}
		
		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side) 
	{
		return false;
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
}
