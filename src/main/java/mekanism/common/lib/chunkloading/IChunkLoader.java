package mekanism.common.lib.chunkloading;

import java.util.Set;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.world.level.ChunkPos;

public interface IChunkLoader {

    TileComponentChunkLoader<?> getChunkLoader();

    Set<ChunkPos> getChunkSet();
}