package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMechanicalPipe extends BlockLargeTransmitter {

    private final PipeTier tier;

    public BlockMechanicalPipe(PipeTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_mechanical_pipe");
        this.tier = tier;
    }

    public PipeTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityMechanicalPipe();
    }
}