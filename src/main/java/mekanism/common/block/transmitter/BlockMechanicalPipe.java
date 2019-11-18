package mekanism.common.block.transmitter;

import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import net.minecraft.tileentity.TileEntityType;

public class BlockMechanicalPipe extends BlockLargeTransmitter implements ITieredBlock<PipeTier>, IHasTileEntity<TileEntityMechanicalPipe> {

    private final PipeTier tier;

    public BlockMechanicalPipe(PipeTier tier) {
        this.tier = tier;
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    public TileEntityType<TileEntityMechanicalPipe> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE.getTileEntityType();
        }
    }
}