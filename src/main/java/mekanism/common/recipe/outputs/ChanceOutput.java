package mekanism.common.recipe.outputs;

import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

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
    public void load(NBTTagCompound nbtTags) {
        primaryOutput = new ItemStack(nbtTags.getCompoundTag("primaryOutput"));
        secondaryOutput = new ItemStack(nbtTags.getCompoundTag("secondaryOutput"));
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
            ItemStack primaryStack = inventory.get(primaryIndex);
            if (primaryStack.isEmpty()) {
                if (doEmit) {
                    inventory.set(primaryIndex, primaryOutput.copy());
                }
            } else if (primaryStack.isItemEqual(primaryOutput) && primaryStack.getCount() + primaryOutput.getCount() <= primaryStack.getMaxStackSize()) {
                if (doEmit) {
                    primaryStack.grow(primaryOutput.getCount());
                }
            } else {
                return false;
            }
        }

        if (hasSecondary() && (!doEmit || checkSecondary())) {
            ItemStack secondaryStack = inventory.get(secondaryIndex);
            if (secondaryStack.isEmpty()) {
                if (doEmit) {
                    inventory.set(secondaryIndex, secondaryOutput.copy());
                }
            } else if (secondaryStack.isItemEqual(secondaryOutput) && secondaryStack.getCount() + primaryOutput.getCount() <= secondaryStack.getMaxStackSize()) {
                if (doEmit) {
                    secondaryStack.grow(secondaryOutput.getCount());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public ChanceOutput copy() {
        return new ChanceOutput(primaryOutput.copy(), secondaryOutput.copy(), secondaryChance);
    }
}