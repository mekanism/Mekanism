package mekanism.generators.common.inventory.container.reactor;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class ReactorLogicAdapterContainer extends MekanismTileContainer<TileEntityReactorLogicAdapter> {

    public ReactorLogicAdapterContainer(int id, PlayerInventory inv, TileEntityReactorLogicAdapter tile) {
        super(GeneratorsContainerTypes.REACTOR_LOGIC_ADAPTER, id, inv, tile);
    }

    public ReactorLogicAdapterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorLogicAdapter.class));
    }

    @Override
    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        //Don't include the player's inventory slots
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.reactor_logic_adapter");
    }
}