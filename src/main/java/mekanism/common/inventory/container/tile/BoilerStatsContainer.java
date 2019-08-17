package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class BoilerStatsContainer extends MekanismTileContainer<TileEntityBoilerCasing> implements IEmptyContainer {

    public BoilerStatsContainer(int id, PlayerInventory inv, TileEntityBoilerCasing tile) {
        super(MekanismContainerTypes.BOILER_STATS, id, inv, tile);
    }

    public BoilerStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBoilerCasing.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new BoilerStatsContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.boiler_stats");
    }
}