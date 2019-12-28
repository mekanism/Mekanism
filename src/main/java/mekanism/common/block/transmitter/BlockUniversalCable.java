package mekanism.common.block.transmitter;

import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.tileentity.TileEntityType;

public class BlockUniversalCable extends BlockSmallTransmitter implements ITieredBlock<CableTier>, IHasTileEntity<TileEntityUniversalCable> {

    private final CableTier tier;

    public BlockUniversalCable(CableTier tier) {
        this.tier = tier;
    }

    @Override
    public CableTier getTier() {
        return tier;
    }

    @Override
    public TileEntityType<TileEntityUniversalCable> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE.getTileEntityType();
        }
    }
}