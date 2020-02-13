package mekanism.common.inventory.container.tile.filter;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class LSFilterSelectContainer extends EmptyTileContainer<TileEntityLogisticalSorter> {

    public LSFilterSelectContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile) {
        super(MekanismContainerTypes.LS_FILTER_SELECT, id, inv, tile);
    }

    public LSFilterSelectContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class));
    }

    @Override
    protected void addContainerTrackers() {
        //NO-OP for now, eventually have this maybe add stuff to sync
    }
}