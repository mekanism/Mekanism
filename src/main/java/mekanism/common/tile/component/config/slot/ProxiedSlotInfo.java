package mekanism.common.tile.component.config.slot;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.gas.GasTank;
import mekanism.api.inventory.slot.IInventorySlot;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ProxiedSlotInfo {

    public static class Energy extends EnergySlotInfo {

    }

    public static class Fluid extends FluidSlotInfo {

        private final Supplier<List<FluidTank>> tankSupplier;

        public Fluid(Supplier<List<FluidTank>> tankSupplier) {
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<FluidTank> getTanks() {
            return tankSupplier.get();
        }
    }

    public static class Gas extends GasSlotInfo {

        private final Supplier<List<GasTank>> tankSupplier;

        public Gas(Supplier<List<GasTank>> tankSupplier) {
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<GasTank> getTanks() {
            return tankSupplier.get();
        }
    }

    public static class Heat extends HeatSlotInfo {

    }

    public static class Inventory extends InventorySlotInfo {

        private final Supplier<List<? extends IInventorySlot>> slotSupplier;

        public Inventory(Supplier<List<? extends IInventorySlot>> slotSupplier) {
            this.slotSupplier = slotSupplier;
        }

        @Override
        public List<? extends IInventorySlot> getSlots() {
            return slotSupplier.get();
        }
    }
}