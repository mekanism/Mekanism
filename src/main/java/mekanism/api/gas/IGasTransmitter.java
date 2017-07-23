package mekanism.api.gas;

import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.transmitters.grid.GasNetwork;
import net.minecraft.tileentity.TileEntity;

/**
 * @deprecated - no longer in use, remains present only for backwards compatibility crash avoidance.
 */
@Deprecated
public interface IGasTransmitter extends IGridTransmitter<IGasHandler, GasNetwork>
{
	public boolean canTransferGasToTube(TileEntity tile);
}
