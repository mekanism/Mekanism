package mekanism.common.block.transmitter;

import java.util.Locale;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;

public class BlockUniversalCable extends BlockSmallTransmitter implements ITieredBlock<CableTier>, IHasTileEntity<TileEntityUniversalCable> {

    private final CableTier tier;

    public BlockUniversalCable(CableTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_universal_cable");
        this.tier = tier;
    }

    @Override
    public CableTier getTier() {
        return tier;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileEntityType<TileEntityUniversalCable> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE;
        }
    }
}