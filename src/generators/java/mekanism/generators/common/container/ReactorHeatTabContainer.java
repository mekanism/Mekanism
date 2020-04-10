package mekanism.generators.common.container;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorHeatTabContainer extends EmptyTileContainer<TileEntityReactorController> {

    public ReactorHeatTabContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_HEAT, id, inv, tile);
    }

    public ReactorHeatTabContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        if (tile != null) {
            tile.addHeatTabContainerTrackers(this);
        }
    }
}