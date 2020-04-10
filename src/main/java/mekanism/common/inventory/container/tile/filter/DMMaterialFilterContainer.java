package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.miner.MMaterialFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMMaterialFilterContainer extends FilterContainer<MMaterialFilter, TileEntityDigitalMiner> {

    public DMMaterialFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_MATERIAL_FILTER, id, inv, tile, index);
    }

    public DMMaterialFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readVarInt());
    }

    @Override
    public MMaterialFilter createNewFilter() {
        return new MMaterialFilter();
    }
}