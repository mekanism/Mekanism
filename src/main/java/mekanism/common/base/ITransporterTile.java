package mekanism.common.base;


import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.common.InventoryNetwork;

import net.minecraft.inventory.IInventory;

public interface ITransporterTile extends ITransmitterTile<IInventory,InventoryNetwork>, IBlockableConnection
{
	public ILogisticalTransporter getTransmitter();
}
