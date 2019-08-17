package mekanism.common.block.interfaces;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;

public interface IHasGui<TILE extends TileEntity> {

    INamedContainerProvider getProvider(TILE tile);
}