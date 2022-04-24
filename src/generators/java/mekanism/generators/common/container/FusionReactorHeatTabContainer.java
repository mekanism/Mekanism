package mekanism.generators.common.container;

import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.world.entity.player.Inventory;

public class FusionReactorHeatTabContainer extends EmptyTileContainer<TileEntityFusionReactorController> {

    public FusionReactorHeatTabContainer(int id, Inventory inv, TileEntityFusionReactorController tile) {
        super(GeneratorsContainerTypes.FUSION_REACTOR_HEAT, id, inv, tile);
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        tile.addHeatTabContainerTrackers(this);
    }
}