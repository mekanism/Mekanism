package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.miner.MTagFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMTagFilterContainer extends FilterContainer<MTagFilter, TileEntityDigitalMiner> {

    public DMTagFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_TAG_FILTER, id, inv, tile, index);
    }

    public DMTagFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readVarInt());
    }

    @Override
    public MTagFilter createNewFilter() {
        return new MTagFilter();
    }
}