package mekanism.generators.common.inventory.container_old;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ContainerHeatGenerator extends ContainerFuelGenerator<TileEntityHeatGenerator> {

    public ContainerHeatGenerator(PlayerInventory inventory, TileEntityHeatGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        return tile.getFuel(slotStack) > 0;
    }
}