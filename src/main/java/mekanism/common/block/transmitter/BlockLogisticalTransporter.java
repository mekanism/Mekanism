package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.block.states.IStateColor;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class BlockLogisticalTransporter extends BlockLargeTransmitter implements IStateColor {

    private final TransporterTier tier;

    public BlockLogisticalTransporter(TransporterTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_logistical_transporter");
        this.tier = tier;
    }

    public TransporterTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityLogisticalTransporter();
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }
}