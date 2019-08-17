package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class MatrixStatsContainer extends MekanismTileContainer<TileEntityInductionCasing> implements IEmptyContainer {

    public MatrixStatsContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.MATRIX_STATS, id, inv, tile);
    }

    public MatrixStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityInductionCasing.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new MatrixStatsContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.matrix_stats");
    }
}