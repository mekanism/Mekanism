package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;

public class BlockRestrictiveTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityRestrictiveTransporter> {

    public BlockRestrictiveTransporter() {
        super(properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor()));
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityRestrictiveTransporter> getTileType() {
        return MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER;
    }
}