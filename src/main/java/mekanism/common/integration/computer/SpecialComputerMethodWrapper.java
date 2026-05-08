package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
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
            return tank.getResource().toStack(tank.amountAsLong());
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static long getCapacity(IChemicalTank tank) {
            //TODO - 26.1: Should this return maximum capacity (as in getLimitAsLong(ChemicalResource.EMPTY)?)
            return tank.getCurrentLimitAsLong();
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static long getNeeded(IChemicalTank tank) {
            return tank.getNeededAsLong();
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IChemicalTank tank) {
            return tank.amountAsLong() / (double) tank.getCurrentLimitAsLong();
        }
    }

    public static class ComputerFluidTankWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodIndex(0)
        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static FluidStack getStack(IFluidTank tank) {
            return tank.getResource().toStack(tank.amount());
        }

        @WrappingComputerMethodIndex(1)
        @WrappingComputerMethodHelp("Get the capacity of the %s.")
        public static long getCapacity(IFluidTank tank) {
            //TODO - 26.1: Should this return maximum capacity (as in getLimitAsLong(FluidResource.EMPTY)?)
            return tank.getCurrentLimitAsLong();
        }

        @WrappingComputerMethodIndex(2)
        @WrappingComputerMethodHelp("Get the amount needed to fill the %s.")
        public static long getNeeded(IFluidTank tank) {
            return tank.getNeededAsLong();
        }

        @WrappingComputerMethodIndex(3)
        @WrappingComputerMethodHelp("Get the filled percentage of the %s.")
        public static double getFilledPercentage(IFluidTank tank) {
            return tank.amountAsLong() / (double) tank.getCurrentLimitAsLong();
        }
    }

    public static class ComputerIInventorySlotWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the contents of the %s.")
        public static ItemStack getStack(IInventorySlot slot) {
            return slot.getResource().toStack(slot.amount());
        }
    }

    public static class ComputerHeatCapacitorWrapper extends SpecialComputerMethodWrapper {

        @WrappingComputerMethodHelp("Get the temperature of the %s in Kelvin.")
        public static double getTemperature(IHeatCapacitor capacitor) {
            return capacitor.getTemperature();
        }
    }
}
