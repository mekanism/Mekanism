package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

//TODO: Rename??
public abstract class FilterEmptyContainer<TILE extends TileEntityMekanism> extends MekanismTileContainer<TILE> implements IEmptyContainer {

    protected FilterEmptyContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TILE tile) {
        super(type, id, inv, tile);
    }
}