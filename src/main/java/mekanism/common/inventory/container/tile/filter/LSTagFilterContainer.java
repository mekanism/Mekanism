package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class LSTagFilterContainer extends FilterContainer<TileEntityLogisticalSorter, TOreDictFilter> {

    public LSTagFilterContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile, int index) {
        super(MekanismContainerTypes.LS_TAG_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (TOreDictFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new TOreDictFilter();
        }
    }

    public LSTagFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class), buf.readInt());
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new LSTagFilterContainer(i, inv, tile, tile.filters.indexOf(filter));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.logistical_sorter_tag_filter");
    }
}