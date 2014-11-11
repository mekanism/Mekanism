package mekanism.common.multipart;

import java.util.List;
import java.util.Set;

import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
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
		@Interface(iface = "cofh.api.energy.IEnergyHandler", modid = "CoFHAPI|energy"),
})
public class PartUniversalCable extends PartTransmitter<EnergyNetwork> implements IStrictEnergyAcceptor, IEnergyHandler
{
	public Tier.CableTier tier;

	public static TransmitterIcons cableIcons = new TransmitterIcons(4, 1);

	public double currentPower = 0;

	public double cacheEnergy = 0;
	public double lastWrite = 0;

	public double drawAmount = 100;

	public PartUniversalCable(Tier.CableTier cableTier)
	{
		tier = cableTier;
	}

	@Override
	public void update()
	{
		if(world().isRemote)
		{
			double targetPower = getTransmitterNetwork().clientEnergyScale;

			if(Math.abs(currentPower - targetPower) > 0.01)
			{
				currentPower = (9*currentPower + targetPower)/10;
			}
		}
		else {
			if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0)
			{
				double last = lastWrite;

				if(last != getSaveShare())
				{
					MekanismUtils.saveChunk(tile());
				}
			}

			if(cacheEnergy > 0)
			{
				getTransmitterNetwork().electricityStored += cacheEnergy;
				cacheEnergy = 0;
			}

				List<ForgeDirection> sides = getConnections(ConnectionType.PULL);
				if(!sides.isEmpty())
				{
					TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(tile());

					for(ForgeDirection side : sides)
					{
						if(connectedOutputters[side.ordinal()] != null)
						{
							TileEntity outputter = connectedOutputters[side.ordinal()];

							if(outputter instanceof ICableOutputter && outputter instanceof IStrictEnergyStorage)
							{
								if(((ICableOutputter)outputter).canOutputTo(side.getOpposite()))
								{
									double received = Math.min(((IStrictEnergyStorage)outputter).getEnergy(), drawAmount);
									double toDraw = received;

									if(received > 0)
									{
										toDraw -= getTransmitterNetwork().emit(received, true);
									}
									((IStrictEnergyStorage)outputter).setEnergy(((IStrictEnergyStorage)outputter).getEnergy() - toDraw);
								}
							}
							else if(MekanismUtils.useRF() && outputter instanceof IEnergyProvider)
							{
								double received = ((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)drawAmount, true) * Mekanism.FROM_TE;
								double toDraw = received;

								if(received > 0)
								{
									toDraw -= getTransmitterNetwork().emit(received, true);
								}
								((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)toDraw, false);
							}
							else if(MekanismUtils.useIC2() && outputter instanceof IEnergySource)
							{
								double received = Math.min(((IEnergySource)outputter).getOfferedEnergy() * Mekanism.FROM_IC2, drawAmount);
								double toDraw = received;

								if(received > 0)
								{
									toDraw -= getTransmitterNetwork().emit(received, true);
								}
								((IEnergySource)outputter).drawEnergy(toDraw * Mekanism.TO_IC2);
							}
						}
					}
				}
		}

		super.update();
	}

	private double getSaveShare()
	{
		return EnergyNetwork.round(getTransmitterNetwork().electricityStored*(1F/getTransmitterNetwork().transmitters.size()));
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return tier.type;
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		cacheEnergy = nbtTags.getDouble("cacheEnergy");
		tier = Tier.CableTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		double toSave = getSaveShare();

		lastWrite = toSave;
		nbtTags.setDouble("cacheEnergy", toSave);
		nbtTags.setInteger("tier", tier.ordinal());
	}

	@Override
	public String getType()
	{
		return "mekanism:universal_cable_" + tier.name().toLowerCase();
	}

	public static void registerIcons(IIconRegister register)
	{
		cableIcons.registerCenterIcons(register, new String[] {"UniversalCableBasic", "UniversalCableAdvanced",
				"UniversalCableElite", "UniversalCableUltimate"});
		cableIcons.registerSideIcons(register, new String[] {"TransmitterSideSmall"});
	}

	@Override
	public void preSingleMerge(EnergyNetwork network)
	{
		network.electricityStored += cacheEnergy;
		cacheEnergy = 0;
	}

	@Override
	public IIcon getCenterIcon()
	{
		return cableIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon()
	{
		return cableIcons.getSideIcon(0);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}

	@Override
	public EnergyNetwork createNetworkFromSingleTransmitter(IGridTransmitter<EnergyNetwork> transmitter)
	{
		return new EnergyNetwork(transmitter);
	}

	@Override
	public EnergyNetwork createNetworkByMergingSet(Set<EnergyNetwork> networks)
	{
		return new EnergyNetwork(networks);
	}

	@Override
	public boolean isValidAcceptor(TileEntity acceptor, ForgeDirection side)
	{
		return CableUtils.getConnections(tile())[side.ordinal()];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if(pass == 0 && MekanismClient.fancyUniversalCableRender)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote)
		{
			getTransmitterNetwork().electricityStored -= lastWrite;
		}

		super.onChunkUnload();
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeededInfo();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlowInfo();
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE, !simulate)*Mekanism.TO_TE);
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return canConnect(from);
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}

	@Override
	@Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_TE);
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

		double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
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
		return getTransmitterNetwork().getCapacity();
	}

	@Override
	public double getEnergy()
	{
		return getTransmitterNetwork().electricityStored;
	}

	@Override
	public void setEnergy(double energy)
	{
		getTransmitterNetwork().electricityStored = energy;
	}
}
