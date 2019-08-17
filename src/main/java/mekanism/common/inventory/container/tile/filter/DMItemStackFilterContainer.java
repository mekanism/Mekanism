package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class DMItemStackFilterContainer extends FilterContainer<TileEntityDigitalMiner, MItemStackFilter> {

    public DMItemStackFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_ITEMSTACK_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (MItemStackFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new MItemStackFilter();
        }
    }

    public DMItemStackFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readInt());
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.digital_miner_itemstack_filter");
    }
}