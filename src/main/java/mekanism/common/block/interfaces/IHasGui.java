package mekanism.common.block.interfaces;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.container.INamedContainerProvider;

public interface IHasGui<TILE extends TileEntityMekanism> {

    INamedContainerProvider getProvider(TILE tile);
}