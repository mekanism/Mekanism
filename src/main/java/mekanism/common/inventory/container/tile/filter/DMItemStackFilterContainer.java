package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

//TODO: Should this be FilterEmptyContainer
public class DMItemStackFilterContainer extends FilterContainer<TileEntityDigitalMiner, MItemStackFilter> {

    public DMItemStackFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_ITEMSTACK_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (MItemStackFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new MItemStackFilter();
        }
    }

    public DMItemStackFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readInt());
    }
}