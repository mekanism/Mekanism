package mekanism.common.inventory.container.tile.filter.select;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class LSFilterSelectContainer extends FilterEmptyContainer<TileEntityLogisticalSorter> {

    public LSFilterSelectContainer(int id, PlayerInventory inv, TileEntityLogisticalSorter tile) {
        super(MekanismContainerTypes.LS_FILTER_SELECT, id, inv, tile);
    }

    public LSFilterSelectContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityLogisticalSorter.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new LSFilterSelectContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.logistical_sorter_filter_select");
    }
}