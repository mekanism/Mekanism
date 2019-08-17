package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.content.miner.MMaterialFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class DMMaterialFilterContainer extends FilterContainer<TileEntityDigitalMiner, MMaterialFilter> {

    public DMMaterialFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_MATERIAL_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (MMaterialFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new MMaterialFilter();
        }
    }

    public DMMaterialFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readInt());
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new DMMaterialFilterContainer(i, inv, tile, tile.filters.indexOf(filter));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.digital_miner_material_filter");
    }
}