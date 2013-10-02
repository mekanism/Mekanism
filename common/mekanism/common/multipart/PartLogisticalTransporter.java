package mekanism.common.multipart;

import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.InventoryNetwork;

public class PartLogisticalTransporter extends PartTransmitter<InventoryNetwork, ItemStack>
{
	@Override
	public String getType()
	{
		return "mekanism:logistical_transporter";
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IInventory;
	}

	@Override
	public InventoryNetwork createNetworkFromSingleTransmitter(ITransmitter<InventoryNetwork, ItemStack> transmitter)
	{
		return new InventoryNetwork(transmitter);
	}

	@Override
	public InventoryNetwork createNetworkByMergingSet(Set<InventoryNetwork> networks)
	{
		return new InventoryNetwork(networks);
	}

}
