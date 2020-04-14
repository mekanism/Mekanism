package mekanism.generators.common.container;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FusionReactorFuelTabContainer extends EmptyTileContainer<TileEntityFusionReactorController> {

    public FusionReactorFuelTabContainer(int id, PlayerInventory inv, TileEntityFusionReactorController tile) {
        super(GeneratorsContainerTypes.FUSION_REACTOR_FUEL, id, inv, tile);
    }

    public FusionReactorFuelTabContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFusionReactorController.class));
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        if (tile != null) {
            tile.addFuelTabContainerTrackers(this);
        }
    }
}