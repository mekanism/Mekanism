package mekanism.common.multipart;

import java.util.Collection;
import java.util.List;

import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.api.energy.EnergyStack;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Tier;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import codechicken.lib.vec.Vector3;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import ic2.api.energy.tile.IEnergySource;

@InterfaceList({
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHCore"),
})
public class PartUniversalCable extends PartTransmitter<EnergyAcceptorWrapper, EnergyNetwork> implements IStrictEnergyAcceptor, IEnergyHandler
{
	public Tier.CableTier tier;

	public static TransmitterIcons cableIcons = new TransmitterIcons(4, 8);

	public double currentPower = 0;
	public double lastWrite = 0;

	public EnergyStack buffer = new EnergyStack(0);

	public PartUniversalCable(Tier.CableTier cableTier)
	{
		super();
		tier = cableTier;
	}

	@Override
	public void update()
	{
		if(world().isRemote)
		{
			double targetPower = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().clientEnergyScale : 0;

			if(Math.abs(currentPower - targetPower) > 0.01)
			{
				currentPower = (9 * currentPower + targetPower) / 10;
			}
		} 
		else {
			if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
			{
				double last = getSaveShare();

				if(last != lastWrite)
				{
					lastWrite = last;
					MekanismUtils.saveChunk(tile());
				}
			}

			List<ForgeDirection> sides = getConnections(ConnectionType.PULL);

			if(!sides.isEmpty())
			{
				TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(tile());
				double canDraw = tier.cableCapacity/10F;

				for(ForgeDirection side : sides)
				{
					if(connectedOutputters[side.ordinal()] != null)
					{
						TileEntity outputter = connectedOutputters[side.ordinal()];

						if(outputter instanceof ICableOutputter && outputter instanceof IStrictEnergyStorage)
						{
							if(((ICableOutputter)outputter).canOutputTo(side.getOpposite()))
							{
								double received = Math.min(((IStrictEnergyStorage)outputter).getEnergy(), canDraw);
								double toDraw = received;

								if(received > 0)
								{
									toDraw -= takeEnergy(received, true);
								}
								
								((IStrictEnergyStorage)outputter).setEnergy(((IStrictEnergyStorage)outputter).getEnergy() - toDraw);
							}
						} 
						else if(MekanismUtils.useRF() && outputter instanceof IEnergyProvider)
						{
							double received = ((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)(canDraw*general.TO_TE), true) * general.FROM_TE;
							double toDraw = received;

							if(received > 0)
							{
								toDraw -= takeEnergy(received, true);
							}

							((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)(toDraw*general.TO_TE), false);
						}
						else if(MekanismUtils.useIC2() && outputter instanceof IEnergySource)
						{
							double received = Math.min(((IEnergySource)outputter).getOfferedEnergy() * general.FROM_IC2, canDraw);
							double toDraw = received;

							if(received > 0)
							{
								toDraw -= takeEnergy(received, true);
							}

							((IEnergySource)outputter).drawEnergy(toDraw * general.TO_IC2);
						}
					}
				}
			}
		}

		super.update();
	}

	private double getSaveShare()
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return EnergyNetwork.round(getTransmitter().getTransmitterNetwork().buffer.amount * (1F / getTransmitter().getTransmitterNetwork().transmitters.size()));
		}
		else {
			return buffer.amount;
		}
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		buffer.amount = nbtTags.getDouble("cacheEnergy");
		if(buffer.amount < 0) buffer.amount = 0;
		tier = Tier.CableTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);
		nbtTags.setDouble("cacheEnergy", lastWrite);
		nbtTags.setInteger("tier", tier.ordinal());
	}

	@Override
	public String getType()
	{
		return "mekanism:universal_cable_" + tier.name().toLowerCase();
	}

	public static void registerIcons(IIconRegister register)
	{
		cableIcons.registerCenterIcons(register, new String[]{"UniversalCableBasic", "UniversalCableAdvanced",
				"UniversalCableElite", "UniversalCableUltimate"});
		cableIcons.registerSideIcons(register, new String[] {"SmallTransmitterVerticalBasic", "SmallTransmitterVerticalAdvanced", "SmallTransmitterVerticalElite", "SmallTransmitterVerticalUltimate",
				"SmallTransmitterHorizontalBasic", "SmallTransmitterHorizontalAdvanced", "SmallTransmitterHorizontalElite", "SmallTransmitterHorizontalUltimate"});
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return cableIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return cableIcons.getSideIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return cableIcons.getSideIcon(4+tier.ordinal());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}

	@Override
	public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks)
	{
		return new EnergyNetwork(networks);
	}

	@Override
	public boolean isValidAcceptor(TileEntity acceptor, ForgeDirection side)
	{
		return CableUtils.isValidAcceptorOnSide(tile(), acceptor, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if(pass == 0 && client.fancyUniversalCableRender)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public EnergyNetwork createNewNetwork()
	{
		return new EnergyNetwork();
	}

	@Override
	public void onChunkUnload()
	{
		takeShare();
		super.onChunkUnload();
	}

	@Override
	public Object getBuffer()
	{
		return buffer;
	}

	@Override
	public void takeShare()
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite;
			buffer.amount = lastWrite;
		}
	}

	@Override
	@Method(modid = "CoFHCore")
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(canReceiveEnergy(from))
		{
			return maxReceive - (int)Math.round(takeEnergy(maxReceive * general.FROM_TE, !simulate) * general.TO_TE);
		}

		return 0;
	}

	@Override
	@Method(modid = "CoFHCore")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	@Method(modid = "CoFHCore")
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return canConnect(from);
	}

	@Override
	@Method(modid = "CoFHCore")
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}

	@Override
	@Method(modid = "CoFHCore")
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getTransmitter().getTransmitterNetwork().getEnergyNeeded() * general.TO_TE);
	}

	@Override
	public int getCapacity()
	{
		return tier.cableCapacity;
	}

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!canReceiveEnergy(side))
		{
			return 0;
		}

		double toUse = Math.min(getMaxEnergy() - getEnergy(), amount);
		setEnergy(getEnergy() + toUse);

		return toUse;
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return getConnectionType(side) == ConnectionType.NORMAL;
	}

	@Override
	public double getMaxEnergy()
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().getCapacity();
		} else
		{
			return getCapacity();
		}
	}

	@Override
	public double getEnergy()
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().buffer.amount;
		} 
		else {
			return buffer.amount;
		}
	}

	@Override
	public void setEnergy(double energy)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			getTransmitter().getTransmitterNetwork().buffer.amount = energy;
		} 
		else {
			buffer.amount = energy;
		}
	}

	public double takeEnergy(double energy, boolean doEmit)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().emit(energy, doEmit);
		}
		else {
			double used = Math.min(getCapacity() - buffer.amount, energy);
			
			if(doEmit)
			{
				buffer.amount += used;
			}
			
			return energy - used;
		}
	}

	@Override
	public EnergyAcceptorWrapper getCachedAcceptor(ForgeDirection side)
	{
		ConnectionType type = connectionTypes[side.ordinal()];

		if(type == ConnectionType.PULL || type == ConnectionType.NONE)
		{
			return null;
		}

		return connectionMapContainsSide(currentAcceptorConnections, side) ? EnergyAcceptorWrapper.get(cachedAcceptors[side.ordinal()]) : null;
	}
}
