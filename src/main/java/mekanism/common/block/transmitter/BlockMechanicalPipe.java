package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityAdvancedMechanicalPipe;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityBasicMechanicalPipe;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityEliteMechanicalPipe;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityUltimateMechanicalPipe;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockMechanicalPipe extends BlockLargeTransmitter implements ITieredBlock<PipeTier>, IHasTileEntity<TileEntityMechanicalPipe> {

    private final PipeTier tier;

    public BlockMechanicalPipe(PipeTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_mechanical_pipe");
        this.tier = tier;
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicMechanicalPipe();
            case ADVANCED:
                return new TileEntityAdvancedMechanicalPipe();
            case ELITE:
                return new TileEntityEliteMechanicalPipe();
            case ULTIMATE:
                return new TileEntityUltimateMechanicalPipe();
        }
        return null;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityMechanicalPipe> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicMechanicalPipe.class;
            case ADVANCED:
                return TileEntityAdvancedMechanicalPipe.class;
            case ELITE:
                return TileEntityEliteMechanicalPipe.class;
            case ULTIMATE:
                return TileEntityUltimateMechanicalPipe.class;
        }
        return null;
    }
}