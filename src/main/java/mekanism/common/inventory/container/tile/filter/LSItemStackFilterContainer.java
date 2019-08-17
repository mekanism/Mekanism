package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

//TODO: Should this be FilterEmptyContainer
public class LSItemStackFilterContainer extends FilterContainer<TileEntityLogisticalSorter, TItemStackFilter> {

    public LSItemStackFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_ITEMSTACK_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (TItemStackFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new TItemStackFilter();
        }
    }

    public LSItemStackFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readInt());
    }
}