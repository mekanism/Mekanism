package mekanism.generators.common.inventory.container_old;

import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;

public class ContainerBioGenerator extends ContainerFuelGenerator<TileEntityBioGenerator> {

    public ContainerBioGenerator(PlayerInventory inventory, TileEntityBioGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        if (tile.getFuel(slotStack) > 0) {
            return true;
        }
        if (FluidRegistry.isFluidRegistered("bioethanol")) {
            return new LazyOptionalHelper<>(FluidUtil.getFluidContained(slotStack)).matches(fluidStack -> fluidStack.getFluid() == FluidRegistry.getFluid("bioethanol"));
        }
        return false;
    }
}