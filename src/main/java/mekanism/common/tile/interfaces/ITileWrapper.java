package mekanism.common.tile.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public interface ITileWrapper {

    BlockPos getBlockPos();

    Level getLevel();

    default GlobalPos getTileGlobalPos() {
        return GlobalPos.of(getDimension(), getBlockPos());
    }

    default @NonNull ResourceKey<Level> getDimension() {
        return getLevel().dimension();
    }

}