package mekanism.api.recipes.outputs;

import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

//TODO: Instead of having things be BiFunctions, have them be anonymous classes that can check:
// the amount of operations there is space for (instead of the current simulation method),
// a way of processing it that takes an int for how many operations worth to remove
public class OutputHelper {

    public static BiFunction<@NonNull ItemStack, Boolean, Boolean> getAddToOutput(@Nonnull NonNullList<ItemStack> inventory, int slot) {
        return (output, simulate) -> applyOutputs(inventory, slot, output, simulate);
    }

    public static BiFunction<@NonNull GasStack, Boolean, Boolean> getAddToOutput(@Nonnull GasTank gasTank) {
        return (output, simulate) -> applyOutputs(gasTank, output, simulate);
    }

    public static IOutputHandler<@NonNull GasStack> getOutputHandler(@Nonnull GasTank gasTank) {
        return new IOutputHandler<@NonNull GasStack>() {

            @Override
            public void handleOutput(@NonNull GasStack toOutput, int operations) {
                if (operations == 0) {
                    //This should not happen
                    return;
                }
                GasStack output = toOutput;
                if (operations > 1) {
                    //If we are doing more than one operation we need to make a copy of our stack and change the amount
                    // that we are using the fill the tank with
                    output = toOutput.copy();
                    output.amount = output.amount * operations;
                }
                //TODO: Do we need any checks about what happens if we failed.
                // THEORETICALLY we should always succeed once we get to this point,
                // because handleOutput has checks before it gets called
                gasTank.receive(output, true);
            }

            @Override
            public int operationsRoomFor(@NonNull GasStack toOutput, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any outputs, just return
                    return 0;
                }
                if (toOutput.amount == 0) {
                    //If the output we want to add is empty we return that we can fit whatever we were told the max is
                    return currentMax;
                }
                //Copy the stack and make it be max size
                GasStack maxOutput = toOutput.copy();
                maxOutput.amount = Integer.MAX_VALUE;
                //Then simulate filling the fluid tank so we can see how much actually can fit
                int amountUsed = gasTank.receive(maxOutput, false);
                //Divide the amount we can actually use by the amount one output operation is equal to,
                // it then gets floored by changing it back to an int as we only care about full usages
                //Note: if the value we were told was the maximum amount is smaller than the amount we calculated
                // then return that instead. This is used so we don't have to do extra checks especially with the
                // potential for integer overflow if we instead multiplied our maxOutput amount by currentMax as
                // long as it wouldn't go past Integer.MAX_VALUE
                return Math.min(amountUsed / toOutput.amount, currentMax);
            }
        };
    }

    public static IOutputHandler<@NonNull FluidStack> getOutputHandler(@Nonnull FluidTank fluidTank) {
        return new IOutputHandler<@NonNull FluidStack>() {

            @Override
            public void handleOutput(@NonNull FluidStack toOutput, int operations) {
                if (operations == 0) {
                    //This should not happen
                    return;
                }
                FluidStack output = toOutput;
                if (operations > 1) {
                    //If we are doing more than one operation we need to make a copy of our stack and change the amount
                    // that we are using the fill the tank with
                    output = toOutput.copy();
                    output.amount = output.amount * operations;
                }
                //TODO: Do we need any checks about what happens if we failed.
                // THEORETICALLY we should always succeed once we get to this point,
                // because handleOutput has checks before it gets called
                fluidTank.fill(output, true);
            }

            @Override
            public int operationsRoomFor(@NonNull FluidStack toOutput, int currentMax) {
                if (currentMax == 0) {
                    //Short circuit that if we already can't perform any outputs, just return
                    return 0;
                }
                if (toOutput.amount == 0) {
                    //If the output we want to add is empty we return that we can fit whatever we were told the max is
                    return currentMax;
                }
                //Copy the stack and make it be max size
                FluidStack maxOutput = toOutput.copy();
                maxOutput.amount = Integer.MAX_VALUE;
                //Then simulate filling the fluid tank so we can see how much actually can fit
                int amountUsed = fluidTank.fill(maxOutput, false);
                //Divide the amount we can actually use by the amount one output operation is equal to,
                // it then gets floored by changing it back to an int as we only care about full usages
                //Note: if the value we were told was the maximum amount is smaller than the amount we calculated
                // then return that instead. This is used so we don't have to do extra checks especially with the
                // potential for integer overflow if we instead multiplied our maxOutput amount by currentMax as
                // long as it wouldn't go past Integer.MAX_VALUE
                return Math.min(amountUsed / toOutput.amount, currentMax);
            }
        };
    }

    public static BiFunction<@NonNull Pair<@NonNull GasStack, @NonNull GasStack>, Boolean, Boolean> getAddToOutput(@Nonnull GasTank leftTank, @Nonnull GasTank rightTank) {
        return (output, simulate) -> {
            if (!applyOutputs(leftTank, output.getRight(), simulate)) {
                return false;
            }
            return applyOutputs(rightTank, output.getRight(), simulate);
        };
    }

    public static BiFunction<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>, Boolean, Boolean> getAddToOutput(@Nonnull GasTank gasTank,
          @Nonnull NonNullList<ItemStack> inventory, int slot) {
        return (output, simulate) -> {
            if (!applyOutputs(gasTank, output.getRight(), simulate)) {
                return false;
            }
            return applyOutputs(inventory, slot, output.getLeft(), simulate);
        };
    }

    public static BiFunction<@NonNull ChanceOutput, Boolean, Boolean> getAddToOutput(@Nonnull NonNullList<ItemStack> inventory, int slot, int secondarySlot) {
        return (output, simulate) -> {
            ItemStack primaryOutput = output.getMainOutput();
            if (!primaryOutput.isEmpty() && !applyOutputs(inventory, slot, primaryOutput, simulate)) {
                //If we cannot apply them we return that we failed
                return false;
            }
            //If we are simulating, then simulate with the max value we could have
            ItemStack secondaryOutput = simulate ? output.getMaxSecondaryOutput() : output.getSecondaryOutput();
            return secondaryOutput.isEmpty() || applyOutputs(inventory, secondarySlot, secondaryOutput, simulate);
        };
    }

    //TODO: Should we add some checks to these like ensuring slot is within the inventory size and stuff
    private static boolean applyOutputs(NonNullList<ItemStack> inventory, int index, ItemStack output, boolean simulate) {
        if (output.isEmpty()) {
            //If we are not trying to add anything there is always room
            return true;
        }
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) {
            if (!simulate) {
                inventory.set(index, output.copy());
            }
            return true;
        } else if (ItemHandlerHelper.canItemStacksStack(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize()) {
            if (!simulate) {
                stack.grow(output.getCount());
            }
            return true;
        }
        return false;
    }

    private static boolean applyOutputs(GasTank gasTank, GasStack output, boolean simulate) {
        //TODO: Can we add some form of quick check for if we are trying to add "empty"
        if (gasTank.canReceive(output.getGas()) && gasTank.getNeeded() >= output.amount) {
            if (!simulate) {
                //Only actually fill it if we are not simulating it as we don't use the number for anything
                // so we don't have to call it again since we just checked if it can receive it
                gasTank.receive(output, true);
            }
            return true;
        }
        return false;
    }
}