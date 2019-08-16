package mekanism.generators.common.inventory.container_old;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerSolarGenerator extends ContainerPassiveGenerator<TileEntitySolarGenerator> {

    public ContainerSolarGenerator(PlayerInventory inventory, TileEntitySolarGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 143, 35));
    }
}