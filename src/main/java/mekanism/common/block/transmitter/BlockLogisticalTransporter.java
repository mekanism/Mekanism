package mekanism.common.block.transmitter;

import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;

public class BlockLogisticalTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityLogisticalTransporterBase>, ITypeBlock {

    private final TransporterTier tier;

    public BlockLogisticalTransporter(TransporterTier tier) {
        this.tier = tier;
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityLogisticalTransporter> getTileType() {
        return switch (tier) {
            case ADVANCED -> MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER;
            case ELITE -> MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER;
            case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER;
            case BASIC -> MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER;
        };
    }
}