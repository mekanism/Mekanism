package mekanism.generators.common.inventory.container;

import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismFluids;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGasGenerator extends ContainerFuelGenerator {

    public ContainerGasGenerator(InventoryPlayer inventory, TileEntityGasGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 17, 35));
        addSlotToContainer(new SlotCharge(tileEntity, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        return slotStack.getItem() instanceof IGasItem;
    }

    @Override
    protected ItemStack handleFuel(ItemStack slotStack, int slotID) {
        if (slotID != 0 && slotID != 1) {
            if (((IGasItem) slotStack.getItem()).getGas(slotStack) != null
                  && ((IGasItem) slotStack.getItem()).getGas(slotStack).getGas() == MekanismFluids.Hydrogen) {
                if (!mergeItemStack(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
            return ItemStack.EMPTY;
        }
        return slotStack;
    }
}