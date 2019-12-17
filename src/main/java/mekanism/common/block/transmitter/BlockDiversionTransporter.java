package mekanism.common.block.transmitter;

import mekanism.api.block.IHasTileEntity;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.tileentity.TileEntityType;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {

    @Override
    public TileEntityType<TileEntityDiversionTransporter> getTileType() {
        return MekanismTileEntityTypes.DIVERSION_TRANSPORTER.getTileEntityType();
    }
}