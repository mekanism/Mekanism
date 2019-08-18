package mekanism.common.block.interfaces;

import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public interface IHasTileEntity<TILE extends TileEntity> {

    //TODO: Replace implementation of this with implementation of getTileType
    @Nullable
    Class<? extends TILE> getTileClass();

    default boolean hasMultipleBlocks() {
        return false;
    }

    /**
     * Only used when hasMultipleBlocks is true
     */
    default String getTileName() {
        return "";
    }

    TileEntityType<TILE> getTileType();
}