package mekanism.generators.common.inventory.container.reactor;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorLogicAdapterContainer extends MekanismTileContainer<TileEntityReactorLogicAdapter> implements IEmptyContainer {

    public ReactorLogicAdapterContainer(int id, PlayerInventory inv, TileEntityReactorLogicAdapter tile) {
        super(GeneratorsContainerTypes.REACTOR_LOGIC_ADAPTER, id, inv, tile);
    }

    public ReactorLogicAdapterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorLogicAdapter.class));
    }
}