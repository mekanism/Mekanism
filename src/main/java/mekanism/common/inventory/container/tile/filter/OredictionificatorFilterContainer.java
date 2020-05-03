package mekanism.common.inventory.container.tile.filter;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class OredictionificatorFilterContainer extends FilterContainer<OredictionificatorFilter, TileEntityOredictionificator> {

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, TileEntityOredictionificator tile, int index) {
        super(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, id, inv, tile, index);
    }

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOredictionificator.class), buf.readVarInt());
    }

    @Override
    public OredictionificatorFilter createNewFilter() {
        return new OredictionificatorFilter();
    }
}