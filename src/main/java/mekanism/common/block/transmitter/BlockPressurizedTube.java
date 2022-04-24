package mekanism.common.block.transmitter;

import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;

public class BlockPressurizedTube extends BlockSmallTransmitter implements ITypeBlock, IHasTileEntity<TileEntityPressurizedTube> {

    private final TubeTier tier;

    public BlockPressurizedTube(TubeTier tier) {
        this.tier = tier;
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityPressurizedTube> getTileType() {
        return switch (tier) {
            case ADVANCED -> MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE;
            case ELITE -> MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE;
            case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE;
            case BASIC -> MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE;
        };
    }
}