package mekanism.api.recipes.outputs;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

@ParametersAreNonnullByDefault
public class OutputHelper {

    private OutputHelper() {
    }

    public static <STACK extends ChemicalStack<?>> IOutputHandler<@NonNull STACK> getOutputHandler(IChemicalTank<?, STACK> tank) {
        return new IOutputHandler<@NonNull STACK>() {

            @Override
            public void handleOutput(@Nonnull STACK toOutput, int operations) {
                OutputHelper.handleOutput(tank, toOutput, operations);
            }

            @Override
            public int operationsRoomFor(@Nonnull STACK toOutput, int currentMax) {
                return OutputHelper.operationsRoomFor(tank, toOutput, currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull FluidStack> getOutputHandler(IExtendedFluidTank fluidTank) {
        return new IOutputHandler<@NonNull FluidStack>() {

            @Override
            public void handleOutput(@Nonnull FluidStack toOutput, int operations) {
                OutputHelper.handleOutput(fluidTank, toOutput, operations);
            }

            @Override
            public int operationsRoomFor(@Nonnull FluidStack toOutput, int currentMax) {
                return OutputHelper.operationsRoomFor(fluidTank, toOutput, currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull ItemStack> getOutputHandler(IInventorySlot inventorySlot) {
        return new IOutputHandler<@NonNull ItemStack>() {

            @Override
            public void handleOutput(@Nonnull ItemStack toOutput, int operations) {
                OutputHelper.handleOutput(inventorySlot, toOutput, operations);
            }

            @Override
            public int operationsRoomFor(@Nonnull ItemStack toOutput, int currentMax) {
                return OutputHelper.operationsRoomFor(inventorySlot, toOutput, currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull ChanceOutput> getOutputHandler(IInventorySlot mainSlot, IInventorySlot secondarySlot) {
        return new IOutputHandler<@NonNull ChanceOutput>() {

            @Override
            public void handleOutput(@Nonnull ChanceOutput toOutput, int operations) {
                OutputHelper.handleOutput(mainSlot, toOutput.getMainOutput(), operations);
                //TODO: Batch this into a single addition call, by looping over and calculating things?
                ItemStack secondaryOutput = toOutput.getSecondaryOutput();
                for (int i = 0; i < operations; i++) {
                    OutputHelper.handleOutput(secondarySlot, secondaryOutput, operations);
                    if (i + 1 < operations) {
                        secondaryOutput = toOutput.nextSecondaryOutput();
                    }
                }
            }

            @Override
            public int operationsRoomFor(@Nonnull ChanceOutput toOutput, int currentMax) {
                currentMax = OutputHelper.operationsRoomFor(mainSlot, toOutput.getMainOutput(), currentMax);
                return OutputHelper.operationsRoomFor(secondarySlot, toOutput.getMaxSecondaryOutput(), currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>> getOutputHandler(IGasTank gasTank, IInventorySlot inventorySlot) {
        return new IOutputHandler<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>>() {

            @Override
            public void handleOutput(@Nonnull Pair<@NonNull ItemStack, @NonNull GasStack> toOutput, int operations) {
                OutputHelper.handleOutput(inventorySlot, toOutput.getLeft(), operations);
                OutputHelper.handleOutput(gasTank, toOutput.getRight(), operations);
            }

            @Override
            public int operationsRoomFor(@Nonnull Pair<@NonNull ItemStack, @NonNull GasStack> toOutput, int currentMax) {
                currentMax = OutputHelper.operationsRoomFor(inventorySlot, toOutput.getLeft(), currentMax);
                return OutputHelper.operationsRoomFor(gasTank, toOutput.getRight(), currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull Pair<@NonNull GasStack, @NonNull GasStack>> getOutputHandler(IGasTank leftTank, IGasTank rightTank) {
        return new IOutputHandler<@NonNull Pair<@NonNull GasStack, @NonNull GasStack>>() {

            @Override
            public void handleOutput(@Nonnull Pair<@NonNull GasStack, @NonNull GasStack> toOutput, int operations) {
                OutputHelper.handleOutput(leftTank, toOutput.getLeft(), operations);
                OutputHelper.handleOutput(rightTank, toOutput.getRight(), operations);
            }

            @Override
            public int operationsRoomFor(@Nonnull Pair<@NonNull GasStack, @NonNull GasStack> toOutput, int currentMax) {
                currentMax = OutputHelper.operationsRoomFor(leftTank, toOutput.getLeft(), currentMax);
                return OutputHelper.operationsRoomFor(rightTank, toOutput.getRight(), currentMax);
            }
        };
    }

    public static <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, STACK> tank, STACK toOutput,
          int operations) {
        if (operations == 0) {
            //This should not happen
            return;
        }
        STACK output = tank.createStack(toOutput, toOutput.getAmount() * operations);
        tank.insert(output, Action.EXECUTE, AutomationType.INTERNAL);
    }

    private static void handleOutput(IExtendedFluidTank fluidTank, FluidStack toOutput, int operations) {
        if (operations == 0) {
            //This should not happen
            return;
        }
        fluidTank.insert(new FluidStack(toOutput, toOutput.getAmount() * operations), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private static void handleOutput(IInventorySlot inventorySlot, ItemStack toOutput, int operations) {
        if (operations == 0 || toOutput.isEmpty()) {
            return;
        }
        ItemStack output = toOutput.copy();
        if (operations > 1) {
            //If we are doing more than one operation we need to make a copy of our stack and change the amount
            // that we are using the fill the tank with
            output.setCount(output.getCount() * operations);
        }
        inventorySlot.insertItem(output, Action.EXECUTE, AutomationType.INTERNAL);
    }

    public static <STACK extends ChemicalStack<?>> int operationsRoomFor(IChemicalTank<?, STACK> tank, STACK toOutput, int currentMax) {
        if (currentMax <= 0 || toOutput.isEmpty()) {
            //Short circuit that if we already can't perform any outputs or the output is empty treat it as being able to fit all
            return currentMax;
        }
        //Copy the stack and make it be max size
        STACK maxOutput = tank.createStack(toOutput, Long.MAX_VALUE);
        //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
        STACK remainder = tank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
        long amountUsed = maxOutput.getAmount() - remainder.getAmount();
        //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
        return Math.min(MathUtils.clampToInt(amountUsed / toOutput.getAmount()), currentMax);
    }

    private static int operationsRoomFor(IExtendedFluidTank fluidTank, FluidStack toOutput, int currentMax) {
        if (currentMax <= 0 || toOutput.isEmpty()) {
            //Short circuit that if we already can't perform any outputs or the output is empty treat it as being able to fit all
            return currentMax;
        }
        //Copy the stack and make it be max size
        FluidStack maxOutput = new FluidStack(toOutput, Integer.MAX_VALUE);
        //Then simulate filling the fluid tank so we can see how much actually can fit
        FluidStack remainder = fluidTank.insert(maxOutput, Action.SIMULATE, AutomationType.INTERNAL);
        int amountUsed = maxOutput.getAmount() - remainder.getAmount();
        //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
        return Math.min(amountUsed / toOutput.getAmount(), currentMax);
    }

    private static int operationsRoomFor(IInventorySlot inventorySlot, ItemStack toOutput, int currentMax) {
        if (currentMax <= 0 || toOutput.isEmpty()) {
            //Short circuit that if we already can't perform any outputs or the output is empty treat it as being able to fit all
            return currentMax;
        }
        ItemStack output = toOutput.copy();
        //Make a cope of the stack we are outputting with its maximum size
        output.setCount(output.getMaxStackSize());
        ItemStack remainder = inventorySlot.insertItem(output, Action.SIMULATE, AutomationType.INTERNAL);
        int amountUsed = output.getCount() - remainder.getCount();
        //Divide the amount we can actually use by the amount one output operation is equal to, capping it at the max we were told about
        return Math.min(amountUsed / toOutput.getCount(), currentMax);
    }
}