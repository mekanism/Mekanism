package buildcraft.api.core;

import java.util.List;

import net.minecraft.util.BlockPos;

/** To be implemented by TileEntities able to provide a path on the world, typically BuildCraft path markers. */
public interface IPathProvider {
    List<BlockPos> getPath();

    /** Remove from the world all objects used to define the path. */
    void removeFromWorld();
}
