package mekanism.common.tile.interfaces;

import mekanism.api.Coord4D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITileWrapper {

    BlockPos getTilePos();

    World getTileWorld();

    default Coord4D getTileCoord() {
        return new Coord4D(getTilePos(), getTileWorld());
    }
}