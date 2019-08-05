package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateColor;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityAdvancedLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityBasicLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityEliteLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityUltimateLogisticalTransporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

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
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicLogisticalTransporter();
            case ADVANCED:
                return new TileEntityAdvancedLogisticalTransporter();
            case ELITE:
                return new TileEntityEliteLogisticalTransporter();
            case ULTIMATE:
                return new TileEntityUltimateLogisticalTransporter();
        }
        return null;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityLogisticalTransporter> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicLogisticalTransporter.class;
            case ADVANCED:
                return TileEntityAdvancedLogisticalTransporter.class;
            case ELITE:
                return TileEntityEliteLogisticalTransporter.class;
            case ULTIMATE:
                return TileEntityUltimateLogisticalTransporter.class;
        }
        return null;
    }
}