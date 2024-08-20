package mekanism.common.tile.component.config.slot;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;

public interface IProxiedSlotInfo extends ISlotInfo {

    class EnergyProxy extends EnergySlotInfo implements IProxiedSlotInfo {

        private final Supplier<List<IEnergyContainer>> containerSupplier;

        public EnergyProxy(boolean canInput, boolean canOutput, Supplier<List<IEnergyContainer>> containerSupplier) {
            super(canInput, canOutput);
            this.containerSupplier = containerSupplier;
        }

        @Override
        public List<IEnergyContainer> getContainers() {
            return containerSupplier.get();
        }
    }

    class FluidProxy extends FluidSlotInfo implements IProxiedSlotInfo {

        private final Supplier<List<IExtendedFluidTank>> tankSupplier;

        public FluidProxy(boolean canInput, boolean canOutput, Supplier<List<IExtendedFluidTank>> tankSupplier) {
            super(canInput, canOutput);
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<IExtendedFluidTank> getTanks() {
            return tankSupplier.get();
        }
    }

    class ChemicalProxy extends ChemicalSlotInfo implements IProxiedSlotInfo {

        private final Supplier<List<IChemicalTank>> tankSupplier;

        public ChemicalProxy(boolean canInput, boolean canOutput, Supplier<List<IChemicalTank>> tankSupplier) {
            super(canInput, canOutput);
            this.tankSupplier = tankSupplier;
        }

        @Override
        public List<IChemicalTank> getTanks() {
            return tankSupplier.get();
        }
    }

    class HeatProxy extends HeatSlotInfo implements IProxiedSlotInfo {

        private final Supplier<List<IHeatCapacitor>> capacitorSupplier;

        public HeatProxy(boolean canInput, boolean canOutput, Supplier<List<IHeatCapacitor>> capacitorSupplier) {
            super(canInput, canOutput);
            this.capacitorSupplier = capacitorSupplier;
        }

        @Override
        public List<IHeatCapacitor> getHeatCapacitors() {
            return capacitorSupplier.get();
        }
    }

    class InventoryProxy extends InventorySlotInfo implements IProxiedSlotInfo {

        private final Supplier<List<IInventorySlot>> slotSupplier;

        public InventoryProxy(boolean canInput, boolean canOutput, Supplier<List<IInventorySlot>> slotSupplier) {
            super(canInput, canOutput);
            this.slotSupplier = slotSupplier;
        }

        @Override
        public List<IInventorySlot> getSlots() {
            return slotSupplier.get();
        }
    }

    @FunctionalInterface
    interface ProxySlotInfoCreator<T> {

        IProxiedSlotInfo create(boolean canInput, boolean canOutput, Supplier<List<T>> supplier);
    }
}