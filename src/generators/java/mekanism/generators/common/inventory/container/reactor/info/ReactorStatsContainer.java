package mekanism.generators.common.inventory.container.reactor.info;

import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ReactorStatsContainer extends ReactorInfoContainer {

    public ReactorStatsContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_STATS, id, inv, tile);
    }

    public ReactorStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }
}