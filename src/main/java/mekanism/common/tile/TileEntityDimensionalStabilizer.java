package mekanism.common.tile;

import java.util.Collections;
import java.util.Set;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader {

    private final TileComponentChunkLoader<TileEntityDimensionalStabilizer> chunkLoaderComponent;

    public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);

        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
    }

    @Override
    public TileComponentChunkLoader<?> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.EMPTY_SET; // TODO: implement this
    }
}
