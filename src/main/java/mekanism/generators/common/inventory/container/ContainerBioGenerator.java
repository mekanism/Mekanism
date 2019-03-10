package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;

public class ContainerBioGenerator extends ContainerFuelGenerator {

    public ContainerBioGenerator(InventoryPlayer inventory, TileEntityBioGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 17, 35));
        addSlotToContainer(new SlotCharge(tileEntity, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (((TileEntityBioGenerator) tileEntity).getFuel(slotStack) > 0) {
            return true;
        }
        if (FluidRegistry.isFluidRegistered("bioethanol")) {
            return FluidUtil.getFluidContained(slotStack) != null
                  && FluidUtil.getFluidContained(slotStack).getFluid() == FluidRegistry.getFluid("bioethanol");
        }
        return false;
    }
}