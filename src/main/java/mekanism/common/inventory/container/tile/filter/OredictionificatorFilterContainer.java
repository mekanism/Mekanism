package mekanism.common.inventory.container.tile.filter;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

//TODO: Should this be FilterEmptyContainer
public class OredictionificatorFilterContainer extends FilterContainer<OredictionificatorFilter, TileEntityOredictionificator> {

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, TileEntityOredictionificator tile, int index) {
        super(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, id, inv, tile, index);
    }

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOredictionificator.class), buf.readInt());
    }

    @Override
    public OredictionificatorFilter createNewFilter() {
        return new OredictionificatorFilter();
    }
}