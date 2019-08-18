package mekanism.common.block.transmitter;

import java.util.Locale;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateColor;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;

public class BlockLogisticalTransporter extends BlockLargeTransmitter implements IStateColor, ITieredBlock<TransporterTier>, IHasTileEntity<TileEntityLogisticalTransporter> {

    private final TransporterTier tier;

    public BlockLogisticalTransporter(TransporterTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_logistical_transporter");
        this.tier = tier;
    }

    @Override
    public TransporterTier getTier() {
        return tier;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileEntityType<TileEntityLogisticalTransporter> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER;
        }
    }
}