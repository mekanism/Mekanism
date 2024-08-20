package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod.WrappingComputerMethodHelp;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod.WrappingComputerMethodIndex;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Helper class to hold classes that then can wrap one return type into multiple methods. Everything in this class must be public, static, and exist on both server and
 * client (no bytecode OnlyIn hacks).
 */
public class SpecialComputerMethodWrapper {

    public static class ComputerChemicalTankWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodIndex(0)
        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static ChemicalStack getStack(IChemicalTank tank) {
            return tank.getStack();
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static long getCapacity(IChemicalTank tank) {
            return tank.getCapacity();
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static long getNeeded(IChemicalTank tank) {
            return tank.getNeeded();
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IChemicalTank tank) {
            return tank.getStored() / (double) tank.getCapacity();
        }
    }

    public static class ComputerFluidTankWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodIndex(0)
        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static FluidStack getStack(IExtendedFluidTank tank) {
            return tank.getFluid();
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static int getCapacity(IExtendedFluidTank tank) {
            return tank.getCapacity();
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static int getNeeded(IExtendedFluidTank tank) {
            return tank.getNeeded();
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IExtendedFluidTank tank) {
            return tank.getFluidAmount() / (double) tank.getCapacity();
        }
    }

    public static class ComputerIInventorySlotWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static ItemStack getStack(IInventorySlot slot) {
            return slot.getStack();
        }
    }

    public static class ComputerHeatCapacitorWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the temperature of the %s in Kelvin.")
        public static double getTemperature(IHeatCapacitor capacitor) {
            return capacitor.getTemperature();
        }
    }
}
