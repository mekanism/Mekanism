package mekanism.api.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public interface IHasTileEntity<TILE extends TileEntity> {

    TileEntityType<TILE> getTileType();
}