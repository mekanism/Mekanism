package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.gas.EnumGas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PartPressurizedTube extends PartTransmitter<GasNetwork, EnumGas>
{
	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube";
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IGasAcceptor;
	}

	@Override
	public GasNetwork createNetworkFromSingleTransmitter(ITransmitter<GasNetwork, EnumGas> transmitter)
	{
		return new GasNetwork(transmitter);
	}

	@Override
	public GasNetwork createNetworkByMergingSet(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
	}
}
