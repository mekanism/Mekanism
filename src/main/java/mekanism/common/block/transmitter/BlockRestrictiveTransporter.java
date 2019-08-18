package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import net.minecraft.tileentity.TileEntityType;

public class BlockRestrictiveTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityRestrictiveTransporter> {

    public BlockRestrictiveTransporter() {
        super("restrictive_transporter");
    }

    @Override
    public TileEntityType<TileEntityRestrictiveTransporter> getTileType() {
        return MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER;
    }
}