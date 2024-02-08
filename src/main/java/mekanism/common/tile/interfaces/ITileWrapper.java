package mekanism.common.tile.interfaces;

import mekanism.api.Chunk3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

public interface ITileWrapper {

    BlockPos getBlockPos();

    Level getLevel();

    default GlobalPos getTileGlobalPos() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    default Chunk3D getTileChunk() {
        return new Chunk3D(getTileGlobalPos());
    }
}