package mekanism.generators.common.inventory.container_old;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.FuelHandler;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGasGenerator extends ContainerFuelGenerator<TileEntityGasGenerator> {

    public ContainerGasGenerator(PlayerInventory inventory, TileEntityGasGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (slotStack.getItem() instanceof IGasItem) {
            GasStack gasStack = ((IGasItem) slotStack.getItem()).getGas(slotStack);
            return gasStack != null && FuelHandler.getFuel(gasStack.getGas()) != null;
        }
        return false;
    }
}