package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

//TODO: Should this be FilterEmptyContainer
public class LSTagFilterContainer extends FilterContainer<TOreDictFilter, TileEntityLogisticalSorter> {

    public LSTagFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_TAG_FILTER, id, inv, tile, index);
    }

    public LSTagFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readInt());
    }

    @Override
    public TOreDictFilter createNewFilter() {
        return new TOreDictFilter();
    }
}