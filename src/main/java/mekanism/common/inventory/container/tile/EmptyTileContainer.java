package mekanism.common.inventory.container.tile;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;

public class EmptyTileContainer<TILE extends TileEntityMekanism> extends MekanismTileContainer<TILE> implements IEmptyContainer {

    public EmptyTileContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, TILE tile) {
        super(type, id, inv, tile);
    }
}