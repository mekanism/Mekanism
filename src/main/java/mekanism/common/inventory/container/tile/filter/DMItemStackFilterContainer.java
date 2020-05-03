package mekanism.common.inventory.container.tile.filter;

import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DMItemStackFilterContainer extends FilterContainer<MItemStackFilter, TileEntityDigitalMiner> {

    public DMItemStackFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_ITEMSTACK_FILTER, id, inv, tile, index);
    }

    public DMItemStackFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readVarInt());
    }

    @Override
    public MItemStackFilter createNewFilter() {
        return new MItemStackFilter();
    }
}