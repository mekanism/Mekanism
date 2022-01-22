package mekanism.common.block.transmitter;

import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;

public class BlockRestrictiveTransporter extends BlockLogisticalTransporterBase<TileEntityRestrictiveTransporter> {

    @Override
    public TileEntityTypeRegistryObject<TileEntityRestrictiveTransporter> getTileType() {
        return MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER;
    }
}