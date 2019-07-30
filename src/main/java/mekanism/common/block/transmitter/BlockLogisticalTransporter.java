package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IColor;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateColor;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityLogisticalTransporter tile = (TileEntityLogisticalTransporter) MekanismUtils.getTileEntitySafe(world, pos);
        if (tile != null) {
            EnumColor color = tile.getRenderColor();
            return state.withProperty(BlockStateHelper.colorProperty, (IColor) (color == null ? EnumColor.NONE : color));
        }
        return state;
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