package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.world.level.material.MapColor;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {

    public BlockDiversionTransporter() {
        super(properties -> properties.mapColor(MapColor.COLOR_ORANGE));
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityDiversionTransporter> getTileType() {
        return MekanismTileEntityTypes.DIVERSION_TRANSPORTER;
    }
}