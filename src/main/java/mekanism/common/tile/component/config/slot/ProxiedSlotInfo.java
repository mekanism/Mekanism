package mekanism.common.tile.component.config.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.gas.GasTank;
import mekanism.api.inventory.slot.IInventorySlot;
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

        private final Supplier<List<GasTank>> tankSupplier;

        public Gas(boolean canInput, boolean canOutput, Supplier<List<GasTank>> tankSupplier) {
            super(canInput, canOutput, Collections.emptyList());
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<GasTank> getTanks() {
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