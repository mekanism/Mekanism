package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod.WrappingComputerMethodHelp;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod.WrappingComputerMethodIndex;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/// Helper class to hold classes that then can wrap one return type into multiple methods. Everything in this class must be public, static, and exist on both server and
/// client (no bytecode OnlyIn hacks).
public class SpecialComputerMethodWrapper {

    public static class ComputerChemicalTankWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodIndex(0)
        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static LargeResourceStack<ChemicalResource> getStack(IChemicalTank tank) {
            return tank.asStack();
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static long getCapacity(IChemicalTank tank) {
            //TODO - 26.1: Should this return maximum capacity (as in capacityAsLong(ChemicalResource.EMPTY)?)
            return tank.capacityAsLong(tank.resource());
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static long getNeeded(IChemicalTank tank) {
            return tank.getNeededAsLong(ChemicalResource.EMPTY);
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IChemicalTank tank) {
            return tank.amountAsLong() / (double) tank.capacityAsLong(tank.resource());
        }
    }

    public static class ComputerFluidTankWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodIndex(0)
        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static LargeResourceStack<FluidResource> getStack(IFluidTank tank) {
            return tank.asStack();
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static long getCapacity(IFluidTank tank) {
            //TODO - 26.1: Should this return maximum capacity (as in capacityAsLong(FluidResource.EMPTY)?)
            return tank.capacityAsLong(tank.resource());
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static long getNeeded(IFluidTank tank) {
            return tank.getNeededAsLong(FluidResource.EMPTY);
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IFluidTank tank) {
            return tank.amountAsLong() / (double) tank.capacityAsLong(tank.resource());
        }
    }

    public static class ComputerIInventorySlotWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static LargeResourceStack<ItemResource> getStack(IInventorySlot slot) {
            return slot.asStack();
        }
    }

    public static class ComputerHeatCapacitorWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the temperature of the %s in Kelvin.")
        public static double getTemperature(IHeatCapacitor capacitor) {
            return capacitor.getTemperature();
        }
    }
}
