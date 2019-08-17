package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class LSModIDFilterContainer extends FilterContainer<TileEntityLogisticalSorter, TModIDFilter> {

    public LSModIDFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_MOD_ID_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (TModIDFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new TModIDFilter();
        }
    }

    public LSModIDFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readInt());
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.logistical_sorter_mod_id_filter");
    }
}