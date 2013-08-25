package mekanism.api;

import net.minecraft.tileentity.TileEntity;

public interface IGasTransmitter extends ITransmitter<GasNetwork>
{
    public boolean canTransferGasToTube(TileEntity tile);
}
