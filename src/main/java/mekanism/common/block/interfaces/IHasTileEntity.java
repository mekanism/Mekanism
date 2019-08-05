package mekanism.common.block.interfaces;

import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;

public interface IHasTileEntity<TILE extends TileEntity> {

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
}