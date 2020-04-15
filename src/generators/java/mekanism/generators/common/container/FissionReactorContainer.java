package mekanism.generators.common.container;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FissionReactorContainer extends EmptyTileContainer<TileEntityFissionReactorCasing> {

    public FissionReactorContainer(int id, PlayerInventory inv, TileEntityFissionReactorCasing tile) {
        super(GeneratorsContainerTypes.FISSION_REACTOR, id, inv, tile);
    }

    public FissionReactorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFissionReactorCasing.class));
    }
}
