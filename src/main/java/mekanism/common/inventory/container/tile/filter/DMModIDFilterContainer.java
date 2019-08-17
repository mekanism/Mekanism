package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class DMModIDFilterContainer extends FilterContainer<TileEntityDigitalMiner, MModIDFilter> {

    public DMModIDFilterContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile, int index) {
        super(MekanismContainerTypes.DM_MOD_ID_FILTER, id, inv, tile);
        if (index >= 0) {
            origFilter = (MModIDFilter) tile.filters.get(index);
            filter = origFilter.clone();
        } else {
            filter = new MModIDFilter();
        }
    }

    public DMModIDFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class), buf.readInt());
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.digital_miner_mod_id_filter");
    }
}