package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockBasic;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInductionProvider extends BlockBasic {

    private final InductionProviderTier tier;

    public BlockInductionProvider(InductionProviderTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_induction_provider");
        this.tier = tier;
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionProvider();
    }
}