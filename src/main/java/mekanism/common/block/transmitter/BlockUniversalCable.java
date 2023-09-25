package mekanism.common.block.transmitter;

import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;

public class BlockUniversalCable extends BlockSmallTransmitter implements ITypeBlock, IHasTileEntity<TileEntityUniversalCable> {

    private final CableTier tier;

    public BlockUniversalCable(CableTier tier) {
        super(properties -> properties.mapColor(tier.getBaseTier().getMapColor()));
        this.tier = tier;
    }

    @Override
    protected BaseTier getBaseTier() {
        return this.tier.getBaseTier();
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityUniversalCable> getTileType() {
        return switch (tier) {
            case ADVANCED -> MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE;
            case ELITE -> MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE;
            case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE;
            case BASIC -> MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE;
        };
    }
}