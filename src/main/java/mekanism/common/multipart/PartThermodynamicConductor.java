package mekanism.common.multipart;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.ColourRGBA;
import mekanism.common.HeatNetwork;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.ConductorTier;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.HeatUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class PartThermodynamicConductor extends PartTransmitter<IHeatTransfer, HeatNetwork> implements IHeatTransfer
{
	public Tier.ConductorTier tier;
	
	public double temperature = 0;
	public double clientTemperature = 0;
	public double heatToAbsorb = 0;

	public PartThermodynamicConductor(Tier.ConductorTier conductorTier)
	{
		super();
		tier = conductorTier;
	}

	@Override
	public HeatNetwork createNewNetwork()
	{
		return new HeatNetwork();
	}

	@Override
	public HeatNetwork createNetworkByMerging(Collection networks)
	{
		return new HeatNetwork(networks);
	}

	@Override
	public int getCapacity()
	{
		return 0;
	}

	@Override
	public Object getBuffer()
	{
		return null;
	}

	@Override
	public void takeShare() {}

    @Override
    public void updateShare() {}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, EnumFacing side)
	{
		if(CapabilityUtils.hasCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()))
		{
			IHeatTransfer transfer = CapabilityUtils.getCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
			return transfer.canConnectHeat(side.getOpposite());
		}
		
		return false;
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.HEAT;
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:thermodynamic_conductor_" + tier.name().toLowerCase());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		temperature = nbtTags.getDouble("temperature");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setDouble("temperature", temperature);
		
		return nbtTags;
	}

	public void sendTemp()
	{
		Coord4D coord = new Coord4D(getPos(), getWorld());
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord, getNetworkedData(new ArrayList())), new Range4D(coord));
	}
	
	@Override
	public IHeatTransfer getCachedAcceptor(EnumFacing side)
	{
		TileEntity tile = getCachedTile(side);
		
		if(CapabilityUtils.hasCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()))
		{
			return CapabilityUtils.getCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
		}
		
		return null;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception 
	{
		temperature = dataStream.readDouble();
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		data.add(temperature);
		
		return data;
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeInt(tier.ordinal());
		
		super.writeUpdatePacket(packet);
	}

	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		tier = ConductorTier.values()[packet.readInt()];
		
		super.readUpdatePacket(packet);
	}

	public ColourRGBA getBaseColour()
	{
		return tier.baseColour;
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return tier.inverseConduction;
	}

	@Override
	public double getInsulationCoefficient(EnumFacing side)
	{
		return tier.inverseConductionInsulation;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += tier.inverseHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		
		if(Math.abs(temperature - clientTemperature) > (temperature / 100))
		{
			clientTemperature = temperature;
			sendTemp();
		}
		
		return temperature;
	}

	@Override
	public boolean canConnectHeat(EnumFacing side)
	{
		return true;
	}

	@Override
	public IHeatTransfer getAdjacent(EnumFacing side)
	{
		if(connectionMapContainsSide(getAllCurrentConnections(), side))
		{
			TileEntity adj = getWorld().getTileEntity(getPos().offset(side));
			
			if(CapabilityUtils.hasCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()))
			{
				return CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
			}
		}
		
		return null;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.HEAT_TRANSFER_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}
	
	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = ConductorTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
}
