package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.entity.player.Inventory;

public class DigitalMinerConfigContainer extends MekanismTileContainer<TileEntityDigitalMiner> {

    public DigitalMinerConfigContainer(int id, Inventory inv, TileEntityDigitalMiner tile) {
        super(MekanismContainerTypes.DIGITAL_MINER_CONFIG, id, inv, tile);
    }

    @Override
    protected int getInventoryXOffset() {
        return super.getInventoryXOffset() + 50;
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 88;
    }

    @Override
    protected void addSlots() {
        //Don't add the tile's slots
    }

    @Override
    protected void addContainerTrackers() {
        tile.addConfigContainerTrackers(this);
    }
}