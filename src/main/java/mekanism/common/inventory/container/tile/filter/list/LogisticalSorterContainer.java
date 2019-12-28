package mekanism.common.inventory.container.tile.filter.list;

import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LogisticalSorterContainer extends FilterEmptyContainer<TileEntityLogisticalSorter> {

    public LogisticalSorterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile) {
        super(MekanismContainerTypes.LOGISTICAL_SORTER, id, inv, tile);
    }

    public LogisticalSorterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class));
    }
}