package mekanism.generators.common.inventory.container.reactor.info;

import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorHeatContainer extends ReactorInfoContainer {

    public ReactorHeatContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_HEAT, id, inv, tile);
    }

    public ReactorHeatContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }
}