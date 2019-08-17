package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this be FilterEmptyContainer
public class OredictionificatorFilterContainer extends FilterContainer<TileEntityOredictionificator> {

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, TileEntityOredictionificator tile) {
        super(MekanismContainerTypes.OREDICTIONIFICATOR_FILTER, id, inv, tile);
    }

    public OredictionificatorFilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOredictionificator.class));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.oredictionificator_filter");
    }
}