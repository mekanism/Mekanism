package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.tileentity.TileEntityType;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {

    @Override
    public TileEntityType<TileEntityDiversionTransporter> getTileType() {
        return MekanismTileEntityTypes.DIVERSION_TRANSPORTER.getTileEntityType();
    }
}