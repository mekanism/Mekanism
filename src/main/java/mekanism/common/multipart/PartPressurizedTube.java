package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.TubeTier;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class PartPressurizedTube extends PartTransmitter<IGasHandler, GasNetwork> implements IGasHandler
{
	public Tier.TubeTier tier = Tier.TubeTier.BASIC;
	
	public float currentScale;

	public GasTank buffer = new GasTank(getCapacity());

	public GasStack lastWrite;
	
	public PartPressurizedTube(Tier.TubeTier tubeTier)
	{
		super();
		tier = tubeTier;
		buffer.setMaxGas(getCapacity());
	}

	@Override
	public void update()
	{
		if(!getWorld().isRemote)
        {
            updateShare();

			IGasHandler[] connectedAcceptors = GasTransmission.getConnectedAcceptors(getPos(), getWorld());

			for(EnumFacing side : getConnections(ConnectionType.PULL))
			{
				if(connectedAcceptors[side.ordinal()] != null)
				{
					IGasHandler container = connectedAcceptors[side.ordinal()];

					if(container != null)
					{
						GasStack received = container.drawGas(side.getOpposite(), tier.tubePullAmount, false);

						if(received != null && received.amount != 0)
						{
							container.drawGas(side.getOpposite(), takeGas(received, true), true);
						}
					}
				}
			}

		}
		else {
			float targetScale = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().gasScale : (float)buffer.getStored()/(float)buffer.getMaxGas();

			if(Math.abs(currentScale - targetScale) > 0.01)
			{
				currentScale = (9 * currentScale + targetScale) / 10;
			}
		}

		super.update();
	}

    @Override
    public void updateShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
        {
            GasStack last = getSaveShare();

            if((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getGas() == last.getGas())) || (last == null && lastWrite != null))
            {
                lastWrite = last;
                markDirty();
            }
        }
    }

	private GasStack getSaveShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null)
		{
			int remain = getTransmitter().getTransmitterNetwork().buffer.amount%getTransmitter().getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitter().getTransmitterNetwork().buffer.amount/getTransmitter().getTransmitterNetwork().transmitters.size();

			if(getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter()))
			{
				toSave += remain;
			}

            return new GasStack(getTransmitter().getTransmitterNetwork().buffer.getGas(), toSave);
		}

		return null;
	}

	@Override
	public void onUnloaded()
	{
		if(!getWorld().isRemote && getTransmitter().hasTransmitterNetwork())
		{
			if(lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null)
			{
				getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

				if(getTransmitter().getTransmitterNetwork().buffer.amount <= 0)
				{
					getTransmitter().getTransmitterNetwork().buffer = null;
				}
			}
		}

		super.onUnloaded();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("tier")) tier = TubeTier.values()[nbtTags.getInteger("tier")];
		buffer.setMaxGas(getCapacity());

		if(nbtTags.hasKey("cacheGas"))
		{
			buffer.setGas(GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas")));
		}
        else {
            buffer.setGas(null);
        }
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

        if(lastWrite != null && lastWrite.amount > 0)
        {
            nbtTags.setTag("cacheGas", lastWrite.write(new NBTTagCompound()));
        }
        else {
            nbtTags.removeTag("cacheGas");
		}

        nbtTags.setInteger("tier", tier.ordinal());
        
        return nbtTags;
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:pressurized_tube_" + tier.name().toLowerCase());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, EnumFacing side)
	{
		return GasTransmission.isValidAcceptorOnSide(tile, side);
	}

	@Override
	public GasNetwork createNewNetwork()
	{
		return new GasNetwork();
	}

	@Override
	public GasNetwork createNetworkByMerging(Collection<GasNetwork> networks)
	{
		return new GasNetwork(networks);
	}

	@Override
	public int getCapacity()
	{
		return tier.tubeCapacity;
	}

	@Override
	public GasStack getBuffer()
	{
		return buffer == null ? null : buffer.getGas();
	}

	@Override
	public void takeShare()
    {
        if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null && lastWrite != null)
        {
            getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
            buffer.setGas(lastWrite);
        }
    }

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL)
		{
			return takeGas(stack, doTransfer);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return false;
	}

	public int takeGas(GasStack gasStack, boolean doEmit)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().emit(gasStack, doEmit);
		}
		else {
			return buffer.receive(gasStack, doEmit);
		}
	}
	
	@Override
	public IGasHandler getCachedAcceptor(EnumFacing side)
	{
		TileEntity tile = getCachedTile(side);
		
		if(CapabilityUtils.hasCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite()))
		{
			return CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, side.getOpposite());
		}
		
		return null;
	}
	
	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = TubeTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		tier = TubeTier.values()[packet.readInt()];
		
		super.readUpdatePacket(packet);
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeInt(tier.ordinal());
		
		super.writeUpdatePacket(packet);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.GAS_HANDLER_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}
}
