package mekanism.common.chunkloading;

import java.util.Set;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.util.math.ChunkPos;

public interface IChunkLoader {

    TileComponentChunkLoader getChunkLoader();

    Set<ChunkPos> getChunkSet();
}
