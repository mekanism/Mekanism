package mekanism.common.inventory.container.tile.filter.select;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.container.tile.filter.FilterEmptyContainer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class DMFilterSelectContainer extends FilterEmptyContainer<TileEntityDigitalMiner> {

    public DMFilterSelectContainer(int id, PlayerInventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DM_FILTER_SELECT, id, inv, tile);
    }

    public DMFilterSelectContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDigitalMiner.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new DMFilterSelectContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.digital_miner_filter_select");
    }
}