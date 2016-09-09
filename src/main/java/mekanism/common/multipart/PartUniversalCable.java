package mekanism.common.multipart;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;

import java.util.Collection;
import java.util.List;

import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.EnergyStack;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.EnergyNetwork;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.CableTier;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

@InterfaceList({
	@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla")
})
public class PartUniversalCable extends PartTransmitter<EnergyAcceptorWrapper, EnergyNetwork> implements IStrictEnergyAcceptor, IEnergyReceiver, IEnergyProvider, ITeslaConsumer
{
	public Tier.CableTier tier;

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
		if(getWorld().isRemote)
		{
			double targetPower = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().clientEnergyScale : 0;

			if(Math.abs(currentPower - targetPower) > 0.01)
			{
				currentPower = (9 * currentPower + targetPower) / 10;
			}
		} 
		else {
			updateShare();

			List<EnumFacing> sides = getConnections(ConnectionType.PULL);

			if(!sides.isEmpty())
			{
				TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(getPos(), getWorld());
				double canDraw = tier.cableCapacity/10F;

				for(EnumFacing side : sides)
				{
					if(connectedOutputters[side.ordinal()] != null)
					{
						TileEntity outputter = connectedOutputters[side.ordinal()];

						if(CapabilityUtils.hasCapability(outputter, Capabilities.CABLE_OUTPUTTER_CAPABILITY, side.getOpposite()) && CapabilityUtils.hasCapability(outputter, Capabilities.ENERGY_STORAGE_CAPABILITY, side.getOpposite()))
						{
							IStrictEnergyStorage storage = CapabilityUtils.getCapability(outputter, Capabilities.ENERGY_STORAGE_CAPABILITY, side.getOpposite());
							double received = Math.min(storage.getEnergy(), canDraw);
							double toDraw = received;

							if(received > 0)
							{
								toDraw -= takeEnergy(received, true);
							}

							storage.setEnergy(storage.getEnergy() - toDraw);
						}
						else if(MekanismUtils.useTesla() && CapabilityUtils.hasCapability(outputter, Capabilities.TESLA_PRODUCER_CAPABILITY, side.getOpposite()))
						{
							ITeslaProducer producer = CapabilityUtils.getCapability(outputter, Capabilities.TESLA_PRODUCER_CAPABILITY, side.getOpposite());
							double received = producer.takePower((long)(canDraw*general.TO_TESLA), true)*general.FROM_TESLA;
							double toDraw = received;
							
							if(received > 0)
							{
								toDraw -= takeEnergy(received, true);
							}
							
							producer.takePower((long)(toDraw*general.TO_TESLA), false);
						}
						else if(MekanismUtils.useRF() && outputter instanceof IEnergyProvider)
						{
							double received = ((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)(canDraw*general.TO_RF), true)*general.FROM_RF;
							double toDraw = received;

							if(received > 0)
							{
								toDraw -= takeEnergy(received, true);
							}

							((IEnergyProvider)outputter).extractEnergy(side.getOpposite(), (int)(toDraw*general.TO_RF), false);
						}
						else if(MekanismUtils.useIC2())
						{
							IEnergyTile tile = EnergyNet.instance.getSubTile(outputter.getWorld(), outputter.getPos());
							
							if(tile instanceof IEnergySource)
							{
								double received = Math.min(((IEnergySource)tile).getOfferedEnergy()*general.FROM_IC2, canDraw);
								double toDraw = received;
	
								if(received > 0)
								{
									toDraw -= takeEnergy(received, true);
								}
	
								((IEnergySource)tile).drawEnergy(toDraw*general.TO_IC2);
							}
						}
					}
				}
			}
		}

		super.update();
	}

    @Override
    public void updateShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
        {
            double last = getSaveShare();

            if(last != lastWrite)
            {
                lastWrite = last;
                markDirty();
            }
        }
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
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		buffer.amount = nbtTags.getDouble("cacheEnergy");
		if(buffer.amount < 0) buffer.amount = 0;

		if(nbtTags.hasKey("tier")) tier = Tier.CableTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setDouble("cacheEnergy", lastWrite);
		nbtTags.setInteger("tier", tier.ordinal());
		
		return nbtTags;
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:universal_cable_" + tier.name().toLowerCase());
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
	public boolean isValidAcceptor(TileEntity acceptor, EnumFacing side)
	{
		return CableUtils.isValidAcceptorOnSide(getWorld().getTileEntity(getPos()), acceptor, side);
	}

	@Override
	public EnergyNetwork createNewNetwork()
	{
		return new EnergyNetwork();
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
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
	{
		return maxReceive - (int)Math.round(takeEnergy(maxReceive*general.FROM_RF, !simulate)*general.TO_RF);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return canConnect(from);
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return (int)Math.round(getEnergy() * general.TO_RF);
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return (int)Math.round(getMaxEnergy() * general.TO_RF);
	}

	@Override
	public int getCapacity()
	{
		return tier.cableCapacity;
	}

	@Override
	public double transferEnergyToAcceptor(EnumFacing side, double amount)
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
	public boolean canReceiveEnergy(EnumFacing side)
	{
		return getConnectionType(side) == ConnectionType.NORMAL;
	}

	@Override
	public double getMaxEnergy()
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().getCapacity();
		} 
		else {
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
	
	@Override
	@Method(modid = "tesla")
	public long givePower(long power, boolean simulated) 
	{
		return power - (long)Math.round(takeEnergy(power*general.FROM_TESLA, !simulated)*general.TO_TESLA);
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
	public EnergyAcceptorWrapper getCachedAcceptor(EnumFacing side)
	{
		return EnergyAcceptorWrapper.get(getCachedTile(side), side.getOpposite());
	}

	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = CableTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		tier = CableTier.values()[packet.readInt()];
		
		super.readUpdatePacket(packet);
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeInt(tier.ordinal());
		
		super.writeUpdatePacket(packet);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == Capabilities.ENERGY_STORAGE_CAPABILITY
				|| capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY
				|| capability == Capabilities.TESLA_CONSUMER_CAPABILITY
				|| super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY ||
				capability == Capabilities.TESLA_CONSUMER_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, facing);
	}
}
