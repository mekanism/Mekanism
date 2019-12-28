package mekanism.common.inventory.container.tile.filter.select;

import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMFilterSelectContainer extends FilterEmptyContainer<TileEntityDigitalMiner> {

    public DMFilterSelectContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DM_FILTER_SELECT, id, inv, tile);
    }

    public DMFilterSelectContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class));
    }
}