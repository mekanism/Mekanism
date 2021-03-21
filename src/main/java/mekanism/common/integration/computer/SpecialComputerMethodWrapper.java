package mekanism.common.integration.computer;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helper class to hold classes that then can wrap one return type into multiple methods. Everything in this class must be public, static, and exist on both server and
 * client (no bytecode OnlyIn hacks).
 */
public class SpecialComputerMethodWrapper {

    public static class ComputerChemicalTankWrapper extends SpecialComputerMethodWrapper {

        public static ChemicalStack<?> getStack(IChemicalTank<?, ?> tank) {
            return tank.getStack();
        }

        public static long getCapacity(IChemicalTank<?, ?> tank) {
            return tank.getCapacity();
        }

        public static long getNeeded(IChemicalTank<?, ?> tank) {
            return tank.getNeeded();
        }

        public static double getFilledPercentage(IChemicalTank<?, ?> tank) {
            return tank.getStored() / (double) tank.getCapacity();
        }
    }

    public static class ComputerFluidTankWrapper extends SpecialComputerMethodWrapper {

        public static FluidStack getStack(IExtendedFluidTank tank) {
            return tank.getFluid();
        }

        public static int getCapacity(IExtendedFluidTank tank) {
            return tank.getCapacity();
        }

        public static int getNeeded(IExtendedFluidTank tank) {
            return tank.getNeeded();
        }

        public static double getFilledPercentage(IExtendedFluidTank tank) {
            return tank.getFluidAmount() / (double) tank.getCapacity();
        }
    }

    public static class ComputerIInventorySlotWrapper extends SpecialComputerMethodWrapper {

        public static ItemStack getStack(IInventorySlot slot) {
            return slot.getStack();
        }
    }

    public static class ComputerHeatCapacitorWrapper extends SpecialComputerMethodWrapper {

        public static double getTemperature(IHeatCapacitor capacitor) {
            return capacitor.getTemperature();
        }
    }
}