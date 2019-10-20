package mekanism.generators.common.inventory.container.reactor;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorControllerContainer extends MekanismTileContainer<TileEntityReactorController> {

    public ReactorControllerContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_CONTROLLER, id, inv, tile);
    }

    public ReactorControllerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }
}