package mekanism.common.multipart;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.List;
import java.util.Set;

import mekanism.api.energy.IStrictEnergyAcceptor;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import codechicken.lib.vec.Vector3;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartUniversalCable extends PartTransmitter<EnergyNetwork> implements IStrictEnergyAcceptor, IEnergyHandler, IPowerReceptor
{
	public Tier.CableTier tier;

	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;

	public static TransmitterIcons cableIcons = new TransmitterIcons(4, 1);

	public double currentPower = 0;

	public double cacheEnergy = 0;
	public double lastWrite = 0;

	public PartUniversalCable(Tier.CableTier cableTier)
	{
		tier = cableTier;
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configurePowerPerdition(0, 0);
		powerHandler.configure(0, 0, 0, 0);
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
				TileEntity[] connectedAcceptors = CableUtils.getConnectedEnergyAcceptors(tile());

				for(ForgeDirection side : sides)
				{
					if(connectedAcceptors[side.ordinal()] != null)
					{
						TileEntity acceptor = connectedAcceptors[side.ordinal()];

						if(acceptor instanceof IEnergySource)
						{
							double received = ((IEnergySource) acceptor).getOfferedEnergy()*Mekanism.FROM_IC2;
							double toDraw = received;

							if(received > 0)
							{
								toDraw -= getTransmitterNetwork().emit(received);
							}
							((IEnergySource) acceptor).drawEnergy(toDraw*Mekanism.TO_IC2);
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
	public void refreshTransmitterNetwork()
	{
		super.refreshTransmitterNetwork();

		reconfigure();
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
		if(pass == 1 && MekanismClient.fancyUniversalCableRender)
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
		return getTransmitterNetwork().getNeeded();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(!simulate)
		{
			return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE)*Mekanism.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from)
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}

	@Override
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
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
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

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if(getTransmitterNetwork().getEnergyNeeded() == 0)
		{
			return null;
		}

		return powerHandler.getPowerReceiver();
	}

	@Override
	public World getWorld()
	{
		return world();
	}

	private void reconfigure()
	{
		if(MekanismUtils.useBuildCraft())
		{
			float needed = (float)(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_BC);
			powerHandler.configure(1, needed, 0, needed);
		}
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{
		if(MekanismUtils.useBuildCraft())
		{
			if(powerHandler.getEnergyStored() > 0)
			{
				getTransmitterNetwork().emit(powerHandler.getEnergyStored()*Mekanism.FROM_BC);
			}

			powerHandler.setEnergy(0);
			reconfigure();
		}
	}
}
