package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {

    @Override
    public TileEntityTypeRegistryObject<TileEntityDiversionTransporter> getTileType() {
        return MekanismTileEntityTypes.DIVERSION_TRANSPORTER;
    }
}