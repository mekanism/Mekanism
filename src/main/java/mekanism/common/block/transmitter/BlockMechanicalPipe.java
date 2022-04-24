package mekanism.common.block.transmitter;

import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;

public class BlockMechanicalPipe extends BlockLargeTransmitter implements ITypeBlock, IHasTileEntity<TileEntityMechanicalPipe> {

    private final PipeTier tier;

    public BlockMechanicalPipe(PipeTier tier) {
        this.tier = tier;
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityMechanicalPipe> getTileType() {
        return switch (tier) {
            case ADVANCED -> MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE;
            case ELITE -> MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE;
            case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE;
            case BASIC -> MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE;
        };
    }
}