package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LSModIDFilterContainer extends FilterContainer<TModIDFilter, TileEntityLogisticalSorter> {

    public LSModIDFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_MOD_ID_FILTER, id, inv, tile, index);
    }

    public LSModIDFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readVarInt());
    }

    @Override
    public TModIDFilter createNewFilter() {
        return new TModIDFilter();
    }
}