package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.entity.player.Inventory;

public class EmptyTileContainer<TILE extends TileEntityMekanism> extends MekanismTileContainer<TILE> implements IEmptyContainer {

    public EmptyTileContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, TILE tile) {
        super(type, id, inv, tile);
    }
}