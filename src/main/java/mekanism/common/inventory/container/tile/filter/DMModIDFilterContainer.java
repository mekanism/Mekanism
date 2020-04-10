package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMModIDFilterContainer extends FilterContainer<MModIDFilter, TileEntityDigitalMiner> {

    public DMModIDFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_MOD_ID_FILTER, id, inv, tile, index);
    }

    public DMModIDFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readInt());
    }

    @Override
    public MModIDFilter createNewFilter() {
        return new MModIDFilter();
    }
}