package mekanism.api.gas;

import mekanism.api.transmitters.ITransmitter;
import net.minecraft.tileentity.TileEntity;

public interface IGasTransmitter extends ITransmitter<GasNetwork>
{
    public boolean canTransferGasToTube(TileEntity tile);
}
