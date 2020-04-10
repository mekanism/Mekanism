package mekanism.generators.common.container;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorFuelTabContainer extends EmptyTileContainer<TileEntityReactorController> {

    public ReactorFuelTabContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_FUEL, id, inv, tile);
    }

    public ReactorFuelTabContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        if (tile != null) {
            tile.addFuelTabContainerTrackers(this);
        }
    }
}