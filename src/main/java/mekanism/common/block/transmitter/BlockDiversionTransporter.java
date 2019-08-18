package mekanism.common.block.transmitter;

import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;

public class BlockDiversionTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityDiversionTransporter> {

    public BlockDiversionTransporter() {
        super("diversion_transporter");
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileEntityType<TileEntityDiversionTransporter> getTileType() {
        return MekanismTileEntityTypes.DIVERSION_TRANSPORTER;
    }
}