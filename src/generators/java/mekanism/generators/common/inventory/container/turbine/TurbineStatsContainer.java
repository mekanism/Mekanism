package mekanism.generators.common.inventory.container.turbine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class TurbineStatsContainer extends MekanismTileContainer<TileEntityTurbineCasing> implements IEmptyContainer {

    public TurbineStatsContainer(int id, PlayerInventory inv, TileEntityTurbineCasing tile) {
        super(GeneratorsContainerTypes.TURBINE_STATS, id, inv, tile);
    }

    public TurbineStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityTurbineCasing.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new TurbineStatsContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.turbine_stats");
    }
}