package mekanism.common.block.interfaces;

import mekanism.common.content.blocktype.BlockTile;
import mekanism.common.tile.base.TileEntityMekanism;

public interface ITypeBlock<TILE extends TileEntityMekanism> {

    public BlockTile<TILE> getType();
}
