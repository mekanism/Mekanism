package mekanism.common.tile.component.config.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IInventorySlot;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ProxiedSlotInfo {

    public static class Energy extends EnergySlotInfo {

        public Energy(boolean canInput, boolean canOutput) {
            super(canInput, canOutput);
        }
    }

    public static class Fluid extends FluidSlotInfo {

        private final Supplier<List<FluidTank>> tankSupplier;

        public Fluid(boolean canInput, boolean canOutput, Supplier<List<FluidTank>> tankSupplier) {
            super(canInput, canOutput, Collections.emptyList());
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<FluidTank> getTanks() {
            return tankSupplier.get();
        }
    }

    public static class Gas extends GasSlotInfo {

        private final Supplier<List<? extends IChemicalTank<mekanism.api.chemical.gas.Gas, GasStack>>> tankSupplier;

        public Gas(boolean canInput, boolean canOutput, Supplier<List<? extends IChemicalTank<mekanism.api.chemical.gas.Gas, GasStack>>> tankSupplier) {
            super(canInput, canOutput, Collections.emptyList());
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<? extends IChemicalTank<mekanism.api.chemical.gas.Gas, GasStack>> getTanks() {
            return tankSupplier.get();
        }
    }

    public static class Heat extends HeatSlotInfo {

        public Heat(boolean canInput, boolean canOutput) {
            super(canInput, canOutput);
        }
    }

    public static class Inventory extends InventorySlotInfo {

        private final Supplier<List<IInventorySlot>> slotSupplier;

        public Inventory(boolean canInput, boolean canOutput, Supplier<List<IInventorySlot>> slotSupplier) {
            super(canInput, canOutput, Collections.emptyList());
            this.slotSupplier = slotSupplier;
        }

        @Override
        public List<IInventorySlot> getSlots() {
            return slotSupplier.get();
        }
    }
}