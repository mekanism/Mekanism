package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LSItemStackFilterContainer extends FilterContainer<TItemStackFilter, TileEntityLogisticalSorter> {

    public LSItemStackFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_ITEMSTACK_FILTER, id, inv, tile, index);
    }

    public LSItemStackFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readInt());
    }

    @Override
    public TItemStackFilter createNewFilter() {
        return new TItemStackFilter();
    }
}