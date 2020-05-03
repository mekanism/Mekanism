package mekanism.common.inventory.container.tile.filter;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMFilterSelectContainer extends EmptyTileContainer<TileEntityDigitalMiner> {

    public DMFilterSelectContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DM_FILTER_SELECT, id, inv, tile);
    }

    public DMFilterSelectContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class));
    }

    @Override
    protected void addContainerTrackers() {
        //NO-OP for now, eventually have this maybe add stuff to sync
    }
}