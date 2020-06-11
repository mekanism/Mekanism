package mekanism.common.block.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public interface IHasTileEntity<TILE extends TileEntity> {

    TileEntityType<? extends TILE> getTileType();
}