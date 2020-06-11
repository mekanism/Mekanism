package mekanism.common.block.transmitter;

import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.tileentity.TileEntityType;

public class BlockLogisticalTransporter extends BlockLargeTransmitter implements ITypeBlock, IHasTileEntity<TileEntityLogisticalTransporterBase> {

    private final TransporterTier tier;

    public BlockLogisticalTransporter(TransporterTier tier) {
        this.tier = tier;
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityType<TileEntityLogisticalTransporterBase> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER.getTileEntityType();
        }
    }
}