package mekanism.generators.common.inventory.container;

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
        addSlot(new Slot(tileEntity, 0, 17, 35));
        addSlot(new SlotCharge(tileEntity, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        return tileEntity.getFuel(slotStack) > 0;
    }
}