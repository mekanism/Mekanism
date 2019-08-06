package mekanism.common.recipe.outputs;

import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

public class ChanceOutput extends MachineOutput<ChanceOutput> {

    private static Random rand = new Random();

    public ItemStack primaryOutput = ItemStack.EMPTY;

    public ItemStack secondaryOutput = ItemStack.EMPTY;

    public double secondaryChance;

    public ChanceOutput(ItemStack primary, ItemStack secondary, double chance) {
        primaryOutput = primary;
        secondaryOutput = secondary;
        secondaryChance = chance;
    }

    public ChanceOutput() {
    }

    public ChanceOutput(ItemStack primary) {
        primaryOutput = primary;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        primaryOutput = new ItemStack(nbtTags.getCompound("primaryOutput"));
        secondaryOutput = new ItemStack(nbtTags.getCompound("secondaryOutput"));
        secondaryChance = nbtTags.getDouble("secondaryChance");
    }

    public boolean checkSecondary() {
        return rand.nextDouble() <= secondaryChance;
    }

    public boolean hasPrimary() {
        return !primaryOutput.isEmpty();
    }

    public boolean hasSecondary() {
        return !secondaryOutput.isEmpty();
    }

    public boolean applyOutputs(NonNullList<ItemStack> inventory, int primaryIndex, int secondaryIndex, boolean doEmit) {
        if (hasPrimary()) {
            if (applyOutputs(inventory, primaryIndex, doEmit, primaryOutput)) {
                return false;
            }
        }
        if (hasSecondary() && (!doEmit || checkSecondary())) {
            return !applyOutputs(inventory, secondaryIndex, doEmit, secondaryOutput);
        }
        return true;
    }

    private boolean applyOutputs(NonNullList<ItemStack> inventory, int index, boolean doEmit, ItemStack output) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) {
            if (doEmit) {
                inventory.set(index, output.copy());
            }
            return false;
        } else if (ItemHandlerHelper.canItemStacksStack(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize()) {
            if (doEmit) {
                stack.grow(output.getCount());
            }
            return false;
        }
        return true;
    }

    @Override
    public ChanceOutput copy() {
        return new ChanceOutput(primaryOutput.copy(), secondaryOutput.copy(), secondaryChance);
    }
}