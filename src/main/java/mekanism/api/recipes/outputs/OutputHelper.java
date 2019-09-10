package mekanism.api.recipes.outputs;

import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

public class OutputHelper {

    public static BiFunction<@NonNull ItemStack, Boolean, Boolean> getAddToOutput(@Nonnull NonNullList<ItemStack> inventory, int slot) {
        return (output, simulate) -> applyOutputs(inventory, slot, output, simulate);
    }

    public static BiFunction<@NonNull GasStack, Boolean, Boolean> getAddToOutput(@Nonnull GasTank gasTank) {
        return (output, simulate) -> {
            if (gasTank.canReceive(output.getGas()) && gasTank.getNeeded() >= output.amount) {
                gasTank.receive(output, !simulate);
                return true;
            }
            return false;
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
}