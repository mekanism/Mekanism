package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class BlockUniversalCable extends BlockSmallTransmitter {

    private final CableTier tier;

    public BlockUniversalCable(CableTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_universal_cable");
        this.tier = tier;
    }

    public CableTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityUniversalCable(tier);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }
}